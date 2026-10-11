(() => {
    const messageDialog = document.querySelector("#server-message-dialog");
    const messageText = document.querySelector("#server-message-text");
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
    const requestedMessageKey = new URLSearchParams(window.location.search).get("messageKey");
    const messageKey = messageKeys.has(requestedMessageKey) ? requestedMessageKey : "server.actionFailed";

    const renderMessage = () => {
        messageText.textContent = translate(messageKey);
    };

    document.addEventListener("r2mod-sync-translations-ready", renderMessage);
    if (window.r2modSyncTranslate) {
        renderMessage();
    }
    messageDialog.showModal();

    window.setTimeout(() => {
        window.location.replace(messageDialog.dataset.returnUrl);
    }, 6000);
})();
