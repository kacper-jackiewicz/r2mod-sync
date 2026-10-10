(() => {
    const controls = document.querySelector(".server-controls");
    if (!controls) {
        return;
    }

    const statusDescription = document.querySelector("#server-status-description");
    const statusBadge = document.querySelector("#server-status-badge");
    const statusIcon = document.querySelector(".server-status-card .workspace-status-icon");
    const defaultStatusIcon = statusIcon.textContent;
    const controlsNote = document.querySelector("#server-controls-note");
    const downloadButton = document.querySelector("#download-current-profile");
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

    const setStatus = (descriptionKey, badgeKey, badgeClass) => {
        statusDescription.textContent = translate(descriptionKey);
        const badgeText = statusBadge.querySelector("[data-i18n]");
        badgeText.textContent = translate(badgeKey);
        statusBadge.className = `server-badge ${badgeClass}`;
        statusIcon.classList.toggle("workspace-status-icon--running", badgeClass === "server-badge--running");
        statusIcon.classList.toggle("workspace-status-icon--stopped", badgeClass === "server-badge--stopped");
        statusIcon.textContent = badgeClass === "server-badge--running"
            ? statusIcon.dataset.runningIcon
            : badgeClass === "server-badge--stopped"
                ? statusIcon.dataset.stoppedIcon
                : defaultStatusIcon;
    };

    const setAvailability = (status) => {
        const running = status === "RUNNING";
        const stopped = status === "STOPPED";
        buttons.get("start").disabled = actionInProgress || !stopped;
        buttons.get("stop").disabled = actionInProgress || !running;
        buttons.get("restart").disabled = actionInProgress || !running;
        downloadButton.disabled = !running;
        controlsNote.textContent = translate(running ? "server.controlsReady" : "server.controlsUnavailable");
    };

    const refreshHealth = async () => {
        try {
            const response = await fetch(controls.dataset.healthUrl, {
                headers: { Accept: "application/json" },
                credentials: "same-origin"
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
            } else {
                throw new Error(`Unknown server status: ${health.status}`);
            }
            setAvailability(health.status);
        } catch (error) {
            setStatus("server.unavailable", "server.statusUnknown", "server-badge--unavailable");
            setAvailability("UNKNOWN");
            console.error(error);
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
            const response = await fetch(controls.dataset[`${action}Url`], {
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

    for (const [action, button] of buttons) {
        button.addEventListener("click", () => sendAction(action));
    }

    document.addEventListener("r2mod-sync-translations-ready", () => {
        setStatus("server.checking", "server.checkingBadge", "");
        refreshHealth();
    }, { once: true });

    if (window.r2modSyncTranslate) {
        setStatus("server.checking", "server.checkingBadge", "");
        refreshHealth();
    }

    window.setInterval(refreshHealth, 15000);
})();
