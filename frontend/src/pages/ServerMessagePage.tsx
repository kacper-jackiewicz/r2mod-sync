import {useEffect, useState} from "react";
import {usePageSettings} from "../hooks/usePageSettings";
import type {Language, OperationStatus, Translate, TranslationKey} from "../types";

const messageKeys = new Set<string>([
    "upload.success",
    "upload.error",
    "upload.requestFailed",
    "restore.success",
    "restore.error",
    "server.actionFailed",
    "server.operationInProgress",
    "server.notRunning"
]);

type ServerMessagePageProps = {
    language: Language;
    t: Translate;
};

export function ServerMessagePage({language, t}: ServerMessagePageProps) {
    usePageSettings(language, "page.serverMessage.title", "workspace-page", t);
    const params = new URLSearchParams(window.location.search);
    const operationId = params.get("operationId");
    const initialKey = params.get("messageKey");
    const [messageKey, setMessageKey] = useState<TranslationKey>(
        initialKey && messageKeys.has(initialKey) ? initialKey as TranslationKey : "server.actionInProgress"
    );
    const [pending, setPending] = useState(false);

    useEffect(() => {
        const dialog = document.querySelector("dialog");
        if (dialog && !dialog.open) dialog.showModal();
        let active = true;
        let returnTimer = 0;
        const wait = (milliseconds: number) => new Promise((resolve) => window.setTimeout(resolve, milliseconds));
        const returnToWorkspace = () => {
            returnTimer = window.setTimeout(() => window.location.replace("/?page=server"), 6000);
        };
        const pollOperation = async () => {
            setPending(true);
            while (active) {
                try {
                    const response = await fetch(`/api/server/operation/${encodeURIComponent(operationId!)}`, {
                        headers: {Accept: "application/json"}, credentials: "same-origin", cache: "no-store"
                    });
                    if (!response.ok) throw new Error(`Server operation status request failed: ${response.status}`);
                    const result = await response.json() as OperationStatus;
                    if (result.state === "PENDING") {
                        if (messageKeys.has(result.messageKey)) setMessageKey(result.messageKey as TranslationKey);
                        await wait(1000);
                        continue;
                    }
                    if (result.state === "COMPLETED" || result.state === "FAILED") {
                        setMessageKey(messageKeys.has(result.messageKey)
                            ? result.messageKey as TranslationKey : "server.actionFailed");
                        setPending(false);
                        returnToWorkspace();
                        return;
                    }
                    throw new Error("Server operation returned an unknown state.");
                } catch (error) {
                    console.error(error);
                    if (error instanceof TypeError) {
                        await wait(1000);
                        continue;
                    }
                    setMessageKey("server.actionFailed");
                    setPending(false);
                    returnToWorkspace();
                    return;
                }
            }
        };
        if (operationId) {
            void pollOperation();
        } else {
            returnToWorkspace();
        }
        return () => {
            active = false;
            window.clearTimeout(returnTimer);
        };
    }, [operationId]);

    return <dialog id="server-message-dialog" aria-labelledby="server-message-title"
                   aria-describedby="server-message-text">
        <article>
            <div id="server-message-progress"
                 className="workspace-status-icon workspace-status-icon--updating server-message-progress"
                 aria-hidden="true" hidden={!pending}>
                <span className="status-icon-updating"><i></i><i></i><i></i></span>
            </div>
            <h2 id="server-message-title">{t("server.messageTitle")}</h2>
            <p id="server-message-text" role="status">{t(messageKey)}</p>
            <p className="server-message-refresh">{t("server.messageRefresh")}</p>
        </article>
    </dialog>;
}
