(() => {
    const controls = document.querySelector(".server-controls");
    if (!controls) {
        return;
    }

    const statusDescription = document.querySelector("#server-status-description");
    const statusBadge = document.querySelector("#server-status-badge");
    const statusIcon = document.querySelector(".server-status-card .workspace-status-icon");
    const controlsNote = document.querySelector("#server-controls-note");
    const processName = document.querySelector("#server-process-name");
    const uploadForm = document.querySelector(".upload-form");
    const uploadControls = [...document.querySelectorAll(".upload-form input, .upload-form button")];
    const uploadNote = document.querySelector("#upload-note");
    const uploadFeedback = document.querySelector("#upload-feedback");
    const restoreButton = document.querySelector("#restore-vanilla");
    const restoreNote = document.querySelector("#restore-note");
    const downloadButton = document.querySelector("#download-current-profile");
    const downloadNote = document.querySelector("#download-note");
    const buttons = new Map(
        [...controls.querySelectorAll("[data-server-action]")].map((button) => [
            button.dataset.serverAction,
            button
        ])
    );
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const translate = (key) => window.r2modSyncTranslate?.(key) ?? key;
    let actionInProgress = false;
    let serverRunning = false;
    let uploadInProgress = false;
    let healthRequestInProgress = false;
    let uploadFeedbackKey;

    const renderUploadFeedback = () => {
        if (!uploadFeedbackKey) {
            return;
        }
        uploadFeedback.textContent = translate(uploadFeedbackKey);
        uploadFeedback.hidden = false;
        const isError = uploadFeedbackKey !== "upload.success";
        uploadFeedback.className = isError ? "status-message error-message" : "upload-feedback";
        uploadFeedback.setAttribute("role", isError ? "alert" : "status");
    };

    const loadServerConfig = async () => {
        try {
            const response = await fetch(controls.dataset.configUrl, {
                headers: { Accept: "application/json" },
                credentials: "same-origin"
            });
            if (!response.ok) {
                throw new Error(`Server config request failed: ${response.status}`);
            }

            const config = await response.json();
            if (typeof config.processName !== "string" || !config.processName) {
                throw new Error("Server config response has no process name.");
            }
            processName.textContent = config.processName;
        } catch (error) {
            processName.textContent = translate("server.processNameUnavailable");
            console.error(error);
        }
    };

    const setStatus = (descriptionKey, badgeKey, badgeClass, statusOnly = false) => {
        statusDescription.textContent = translate(descriptionKey);
        const badgeText = statusBadge.querySelector("[data-i18n]");
        badgeText.textContent = translate(badgeKey);
        statusBadge.className = `server-badge ${badgeClass}`;
        statusIcon.classList.toggle("workspace-status-icon--running", badgeClass === "server-badge--running");
        statusIcon.classList.toggle("workspace-status-icon--stopped", badgeClass === "server-badge--stopped");
        statusIcon.classList.toggle("workspace-status-icon--updating", badgeClass === "server-badge--updating");
        document.body.classList.toggle("server-status-only", statusOnly);
    };

    const setAvailability = (status) => {
        const running = status === "RUNNING";
        const stopped = status === "STOPPED";
        serverRunning = running;
        document.body.classList.toggle("server-profile-hidden", !running);
        buttons.get("start").disabled = actionInProgress || !stopped;
        buttons.get("stop").disabled = actionInProgress || !running;
        buttons.get("restart").disabled = actionInProgress || !running;
        uploadControls.forEach((control) => {
            control.disabled = !running || uploadInProgress;
        });
        uploadNote.textContent = translate(running ? "upload.noteReady" : "upload.noteUnavailable");
        restoreButton.disabled = !running;
        restoreNote.textContent = translate(running ? "restore.noteReady" : "restore.noteUnavailable");
        downloadButton.setAttribute("aria-disabled", String(!running));
        downloadButton.tabIndex = running ? 0 : -1;
        downloadButton.classList.toggle("profile-download-link--disabled", !running);
        downloadNote.textContent = translate(running ? "download.noteReady" : "download.noteUnavailable");
        controlsNote.textContent = translate(running ? "server.controlsReady" : "server.controlsUnavailable");
    };

    const refreshHealth = async () => {
        if (healthRequestInProgress || document.hidden) {
            return;
        }

        healthRequestInProgress = true;
        try {
            const response = await fetch(controls.dataset.healthUrl, {
                headers: { Accept: "application/json" },
                credentials: "same-origin",
                cache: "no-store"
            });
            if (!response.ok) {
                throw new Error(`Health request failed: ${response.status}`);
            }

            const health = await response.json();
            if (health.status === "NOT_CONFIGURED") {
                setStatus("server.statusDescription", "server.status", "server-badge--unavailable");
            } else if (health.status === "RUNNING") {
                setStatus("server.running", "server.statusRunning", "server-badge--running");
            } else if (health.status === "STOPPED") {
                setStatus("server.stopped", "server.statusStopped", "server-badge--stopped");
            } else if (health.status === "UPDATING") {
                setStatus("server.updating", "server.statusUpdating", "server-badge--updating", true);
            } else {
                throw new Error(`Unknown server status: ${health.status}`);
            }
            setAvailability(health.status);
        } catch (error) {
            setStatus("server.unavailable", "server.statusUnknown", "server-badge--unavailable", true);
            setAvailability("UNKNOWN");
            console.error(error);
        } finally {
            healthRequestInProgress = false;
        }
    };

    const sendAction = async (action) => {
        const button = buttons.get(action);
        if (!button || button.disabled || actionInProgress) {
            return;
        }

        actionInProgress = true;
        for (const controlButton of buttons.values()) {
            controlButton.disabled = true;
        }

        try {
            const headers = { Accept: "application/json" };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            const response = await fetch(button.dataset.actionUrl, {
                method: "POST",
                headers,
                credentials: "same-origin"
            });
            if (!response.ok) {
                throw new Error(`Server ${action} request failed: ${response.status}`);
            }
        } catch (error) {
            controlsNote.textContent = translate("server.actionFailed");
            console.error(error);
        } finally {
            actionInProgress = false;
            await refreshHealth();
        }
    };

    uploadForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!serverRunning || uploadInProgress) {
            return;
        }

        const formData = new FormData(uploadForm);
        uploadInProgress = true;
        uploadControls.forEach((control) => {
            control.disabled = true;
        });
        uploadFeedback.hidden = true;

        try {
            const headers = { Accept: "application/json" };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            const response = await fetch(uploadForm.dataset.uploadUrl, {
                method: "POST",
                headers,
                body: formData,
                credentials: "same-origin"
            });
            const result = await response.json();
            if (typeof result.messageKey !== "string") {
                throw new Error("Upload response has no message key.");
            }

            uploadFeedbackKey = result.messageKey;
            renderUploadFeedback();
        } catch (error) {
            uploadFeedbackKey = "upload.requestFailed";
            renderUploadFeedback();
            console.error(error);
        } finally {
            uploadInProgress = false;
            setAvailability(serverRunning ? "RUNNING" : "UNKNOWN");
        }
    });

    for (const [action, button] of buttons) {
        button.addEventListener("click", () => sendAction(action));
    }

    document.addEventListener("r2mod-sync-translations-ready", () => {
        setStatus("server.checking", "server.checkingBadge", "");
        renderUploadFeedback();
        loadServerConfig();
        refreshHealth();
    }, { once: true });

    if (window.r2modSyncTranslate) {
        setStatus("server.checking", "server.checkingBadge", "");
        renderUploadFeedback();
        loadServerConfig();
        refreshHealth();
    }

    window.setInterval(refreshHealth, 5000);
    document.addEventListener("visibilitychange", () => {
        if (!document.hidden) {
            refreshHealth();
        }
    });
})();
