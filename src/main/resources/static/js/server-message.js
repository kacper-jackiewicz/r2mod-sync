(() => {
    const messageDialog = document.querySelector("#server-message-dialog");
    const messageText = document.querySelector("#server-message-text");
    const progressIcon = document.querySelector("#server-message-progress");
    const translate = (key) => window.r2modSyncTranslate?.(key) ?? key;
    const messageKeys = new Set([
        "upload.success",
        "upload.error",
        "upload.requestFailed",
        "restore.success",
        "restore.error",
        "server.actionFailed",
        "server.operationInProgress",
        "server.notRunning"
    ]);
    const params = new URLSearchParams(window.location.search);
    const operationId = params.get("operationId");
    let messageKey = messageKeys.has(params.get("messageKey")) ? params.get("messageKey") : "server.actionInProgress";

    const renderMessage = () => {
        messageText.textContent = translate(messageKey);
        progressIcon.hidden = messageKey !== "server.operationInProgress";
    };

    document.addEventListener("r2mod-sync-translations-ready", renderMessage);
    if (window.r2modSyncTranslate) {
        renderMessage();
    }
    messageDialog.showModal();

    const returnToWorkspace = () => {
        window.setTimeout(() => {
            window.location.replace(messageDialog.dataset.returnUrl);
        }, 6000);
    };

    const pollOperation = async () => {
        const operationUrl = new URL(
            `${messageDialog.dataset.operationUrl}/${encodeURIComponent(operationId)}`,
            window.location.href
        );
        const waitForNextPoll = () => new Promise((resolve) => window.setTimeout(resolve, 1000));

        while (true) {
            try {
                const response = await fetch(operationUrl, {
                    headers: {Accept: "application/json"},
                    credentials: "same-origin",
                    cache: "no-store"
                });
                if (!response.ok) {
                    throw new Error(`Server operation status request failed: ${response.status}`);
                }

                const result = await response.json();
                if (result.state === "PENDING") {
                    messageKey = messageKeys.has(result.messageKey)
                        ? result.messageKey
                        : "server.actionInProgress";
                    renderMessage();
                    await waitForNextPoll();
                    continue;
                }
                if (result.state === "COMPLETED" || result.state === "FAILED") {
                    messageKey = messageKeys.has(result.messageKey) ? result.messageKey : "server.actionFailed";
                    renderMessage();
                    progressIcon.hidden = true;
                    returnToWorkspace();
                    return;
                }
                throw new Error("Server operation returned an unknown state.");
            } catch (error) {
                console.error(error);
                if (error instanceof TypeError) {
                    await waitForNextPoll();
                    continue;
                }
                messageKey = "server.actionFailed";
                renderMessage();
                progressIcon.hidden = true;
                returnToWorkspace();
                return;
            }

            await waitForNextPoll();
        }
    };

    if (operationId) {
        pollOperation();
    } else {
        renderMessage();
        returnToWorkspace();
    }
})();
