import {useCallback, useEffect, useRef, useState} from "react";
import type {FormEvent, MouseEvent} from "react";
import {Header} from "../components/Header";
import {usePageSettings} from "../hooks/usePageSettings";
import type {PageProps, ServerStatus, TranslationKey} from "../types";
import {readCsrfToken} from "../utils/csrf";
import {showMessage, showResponseMessage} from "../utils/serverMessages";

function StatusIcon({status}: {status: ServerStatus}) {
    const statusClass = status === "RUNNING" ? "workspace-status-icon--running"
        : status === "STOPPED" ? "workspace-status-icon--stopped"
            : status === "UPDATING" ? "workspace-status-icon--updating" : "";
    return <div className={`workspace-status-icon ${statusClass}`} aria-hidden="true">
        <span className="status-icon-running">✔</span>
        <span className="status-icon-stopped">X</span>
        <span className="status-icon-updating"><i></i><i></i><i></i></span>
        <span className="status-icon-default">⋯</span>
    </div>;
}

export function ServerPage({language, setLanguage, t, theme, toggleTheme}: PageProps) {
    usePageSettings(language, "page.server.title", "workspace-page", t);
    const [status, setStatus] = useState<ServerStatus>("CHECKING");
    const [processName, setProcessName] = useState(t("server.processNameLoading"));
    const [actionInProgress, setActionInProgress] = useState(false);
    const [uploadInProgress, setUploadInProgress] = useState(false);
    const [controlsNote, setControlsNote] = useState<TranslationKey>("server.controlsNote");
    const healthRequestInProgress = useRef(false);
    const serverRunning = status === "RUNNING";
    const stopped = status === "STOPPED";

    const refreshHealth = useCallback(async () => {
        if (healthRequestInProgress.current || document.hidden) return;
        healthRequestInProgress.current = true;
        try {
            const response = await fetch("/api/server/health", {
                headers: {Accept: "application/json"}, credentials: "same-origin", cache: "no-store"
            });
            await showResponseMessage(response);
            if (!response.ok) throw new Error(`Health request failed: ${response.status}`);
            const responseBody = await response.text();
            let nextStatus: unknown;
            try {
                nextStatus = JSON.parse(responseBody);
            } catch {
                nextStatus = responseBody.trim();
            }
            if (typeof nextStatus !== "string") throw new Error("Health response must contain a server status string.");
            if (nextStatus === "NOT_CONFIGURED" || nextStatus === "RUNNING"
                || nextStatus === "STOPPED" || nextStatus === "UPDATING") {
                setStatus(nextStatus);
                return;
            }
            throw new Error(`Unknown server status: ${nextStatus}`);
        } catch (error) {
            setStatus("UNKNOWN");
            console.error(error);
        } finally {
            healthRequestInProgress.current = false;
        }
    }, []);

    useEffect(() => {
        let active = true;
        fetch("/api/server/config", {
            headers: {Accept: "application/json"}, credentials: "same-origin"
        }).then(async (response) => {
            await showResponseMessage(response);
            if (!response.ok) throw new Error(`Server config request failed: ${response.status}`);
            const config = await response.json() as {processName?: unknown};
            if (typeof config.processName !== "string" || !config.processName) {
                throw new Error("Server config response has no process name.");
            }
            if (active) setProcessName(config.processName);
        }).catch((error: unknown) => {
            if (active) setProcessName(t("server.processNameUnavailable"));
            console.error(error);
        });
        void refreshHealth();
        const interval = window.setInterval(() => void refreshHealth(), 5000);
        const onVisibilityChange = () => {
            if (!document.hidden) void refreshHealth();
        };
        document.addEventListener("visibilitychange", onVisibilityChange);
        return () => {
            active = false;
            window.clearInterval(interval);
            document.removeEventListener("visibilitychange", onVisibilityChange);
        };
    }, [refreshHealth, t]);

    const sendAction = async (action: "start" | "stop" | "restart") => {
        if (actionInProgress) return;
        setActionInProgress(true);
        try {
            const response = await fetch(`/api/server/${action}`, {
                method: "POST",
                headers: {Accept: "application/json", "X-XSRF-TOKEN": readCsrfToken()},
                credentials: "same-origin"
            });
            const hasMessage = await showResponseMessage(response);
            if (!response.ok) {
                if (!hasMessage) setControlsNote("server.operationInProgress");
                throw new Error(`Server ${action} request failed: ${response.status}`);
            }
        } catch (error) {
            console.error(error);
        } finally {
            setActionInProgress(false);
            await refreshHealth();
        }
    };

    const restoreVanilla = async () => {
        if (!serverRunning || actionInProgress) return;
        setActionInProgress(true);
        let operationAccepted = false;
        try {
            const response = await fetch("/api/server/revert", {
                method: "POST",
                headers: {Accept: "application/json", "X-XSRF-TOKEN": readCsrfToken()},
                credentials: "same-origin"
            });
            const hasMessage = await showResponseMessage(response);
            operationAccepted = response.status === 202 && hasMessage;
            if (!response.ok || !hasMessage) {
                if (!hasMessage) showMessage("server.actionFailed");
                throw new Error(`Vanilla restore request failed: ${response.status}`);
            }
        } catch (error) {
            console.error(error);
        } finally {
            setActionInProgress(false);
            if (!operationAccepted) await refreshHealth();
        }
    };

    const uploadProfile = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (!serverRunning || uploadInProgress) return;
        setUploadInProgress(true);
        try {
            const response = await fetch("/api/server/upload", {
                method: "POST",
                headers: {Accept: "application/json", "X-XSRF-TOKEN": readCsrfToken()},
                body: new FormData(event.currentTarget),
                credentials: "same-origin"
            });
            const hasMessage = await showResponseMessage(response);
            if (!hasMessage) throw new Error("Upload response has no message key.");
        } catch (error) {
            showMessage("upload.requestFailed");
            console.error(error);
        } finally {
            setUploadInProgress(false);
        }
    };

    const downloadProfile = async (event: MouseEvent<HTMLAnchorElement>) => {
        event.preventDefault();
        if (!serverRunning) return;
        const link = event.currentTarget;
        link.setAttribute("aria-busy", "true");
        try {
            const response = await fetch(link.href, {
                headers: {Accept: "application/octet-stream, application/json"}, credentials: "same-origin"
            });
            const hasMessage = await showResponseMessage(response);
            if (hasMessage) return;
            if (!response.ok) {
                showMessage("server.actionFailed");
                throw new Error(`Profile download request failed: ${response.status}`);
            }
            const disposition = response.headers.get("Content-Disposition");
            const filename = disposition?.match(/filename="?([^";]+)"?/i)?.[1] ?? "current.r2z";
            const downloadUrl = URL.createObjectURL(await response.blob());
            const downloadLink = document.createElement("a");
            downloadLink.href = downloadUrl;
            downloadLink.download = filename;
            downloadLink.click();
            window.setTimeout(() => URL.revokeObjectURL(downloadUrl), 1000);
        } catch (error) {
            console.error(error);
        } finally {
            link.removeAttribute("aria-busy");
        }
    };

    const statusDescription: Record<ServerStatus, TranslationKey> = {
        CHECKING: "server.checking",
        NOT_CONFIGURED: "server.statusDescription",
        RUNNING: "server.running",
        STOPPED: "server.stopped",
        UPDATING: "server.updating",
        UNKNOWN: "server.unavailable"
    };
    const badgeKey: Record<ServerStatus, TranslationKey> = {
        CHECKING: "server.checkingBadge",
        NOT_CONFIGURED: "server.status",
        RUNNING: "server.statusRunning",
        STOPPED: "server.statusStopped",
        UPDATING: "server.statusUpdating",
        UNKNOWN: "server.statusUnknown"
    };
    const badgeClass = status === "RUNNING" ? "server-badge--running"
        : status === "STOPPED" ? "server-badge--stopped"
            : status === "UPDATING" ? "server-badge--updating"
                : status === "NOT_CONFIGURED" || status === "UNKNOWN" ? "server-badge--unavailable" : "";
    const statusOnly = status === "UPDATING" || status === "UNKNOWN";

    useEffect(() => {
        document.body.classList.toggle("server-status-only", statusOnly);
        document.body.classList.toggle("server-profile-hidden", !serverRunning);
        return () => {
            document.body.classList.remove("server-status-only", "server-profile-hidden");
        };
    }, [language, serverRunning, statusOnly]);

    useEffect(() => {
        setControlsNote(status === "RUNNING" || status === "STOPPED"
            ? "server.controlsReady" : "server.controlsNote");
    }, [status]);

    return <>
        <Header language={language} setLanguage={setLanguage} t={t} theme={theme} toggleTheme={toggleTheme} signedIn/>
        <main className="workspace-main">
            <section className="workspace-heading">
                <p className="eyebrow">{t("server.eyebrow")}</p>
                <h1>{t("server.title")}</h1>
                <p>{t("server.description")}</p>
            </section>
            <article className="workspace-card server-status-card">
                <StatusIcon status={status}/>
                <div className="workspace-status-copy">
                    <h2>{processName}</h2>
                    <p className="status-message">{t(statusDescription[status])}</p>
                </div>
                <span className={`server-badge ${badgeClass}`}>
                    <span></span><span>{t(badgeKey[status])}</span>
                </span>
                <div className="server-controls" role="group" aria-label={t("server.controls")}>
                    <div className="server-control-buttons">
                        <button type="button" className="secondary" disabled={actionInProgress || !stopped}
                                onClick={() => void sendAction("start")}>{t("server.start")}</button>
                        <button type="button" className="secondary" disabled={actionInProgress || !serverRunning}
                                onClick={() => void sendAction("stop")}>{t("server.stop")}</button>
                        <button type="button" className="secondary" disabled={actionInProgress || !serverRunning}
                                onClick={() => void sendAction("restart")}>{t("server.restart")}</button>
                    </div>
                    <p>{t(controlsNote)}</p>
                </div>
            </article>
            <section className="upload-section" aria-labelledby="upload-title">
                <div className="upload-heading">
                    <p className="eyebrow">{t("upload.eyebrow")}</p>
                    <h2 id="upload-title">{t("upload.title")}</h2>
                    <p><span>{t("upload.description")}</span> <code>.r2z</code> <span>{t("upload.descriptionEnd")}</span></p>
                </div>
                <form className="upload-form" onSubmit={(event) => void uploadProfile(event)}>
                    <label htmlFor="r2z-file">{t("upload.fileLabel")}
                        <input id="r2z-file" name="file" type="file" accept=".r2z,application/octet-stream"
                               required disabled={!serverRunning || uploadInProgress}/>
                    </label>
                    <button type="submit" disabled={!serverRunning || uploadInProgress}>
                        <span>{t("upload.submit")}</span> <span className="button-arrow" aria-hidden="true">↑</span>
                    </button>
                </form>
                <p className="upload-note">{t(serverRunning ? "upload.noteReady" : "upload.noteUnavailable")}</p>
            </section>
            <section className="restore-section" aria-labelledby="restore-title">
                <div className="restore-heading">
                    <p className="eyebrow">{t("restore.eyebrow")}</p>
                    <h2 id="restore-title">{t("restore.title")}</h2>
                    <p>{t("restore.description")}</p>
                </div>
                <div className="profile-download">
                    <button type="button" className="secondary" disabled={!serverRunning || actionInProgress}
                            aria-describedby="restore-note" onClick={() => void restoreVanilla()}>
                        <span>{t("restore.button")}</span>
                    </button>
                    <p id="restore-note">{t(serverRunning ? "restore.noteReady" : "restore.noteUnavailable")}</p>
                </div>
            </section>
            <section className="download-section" aria-labelledby="download-title">
                <div className="download-heading">
                    <p className="eyebrow">{t("download.eyebrow")}</p>
                    <h2 id="download-title">{t("download.title")}</h2>
                    <p>{t("download.description")}</p>
                </div>
                <div className="profile-download">
                    <a id="download-current-profile"
                       className={`secondary${serverRunning ? "" : " profile-download-link--disabled"}`}
                       role="button" href="/api/server/download" aria-disabled={!serverRunning}
                       aria-describedby="download-note" tabIndex={serverRunning ? 0 : -1}
                       onClick={(event) => void downloadProfile(event)}>
                        <span>{t("download.button")}</span>
                    </a>
                    <p id="download-note">{t(serverRunning ? "download.noteReady" : "download.noteUnavailable")}</p>
                </div>
            </section>
            <footer className="workspace-actions">
                <form action="/logout" method="post">
                    <input type="hidden" name="_csrf" value={readCsrfToken()}/>
                    <button type="submit" className="secondary">{t("nav.signOut")}</button>
                </form>
            </footer>
        </main>
    </>;
}
