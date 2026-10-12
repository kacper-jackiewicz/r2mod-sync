import {Header} from "../components/Header";
import {usePageSettings} from "../hooks/usePageSettings";
import {readCsrfToken} from "../utils/csrf";
import type {PageProps} from "../types";

export function LoginPage({language, setLanguage, t, theme, toggleTheme}: PageProps) {
    usePageSettings(language, "page.login.title", "auth-page", t);
    const csrfToken = readCsrfToken();
    const query = new URLSearchParams(window.location.search);

    return <>
        <Header language={language} setLanguage={setLanguage} t={t} theme={theme} toggleTheme={toggleTheme}/>
        <main className="auth-main">
            <article className="auth-card">
                <div className="auth-card-heading">
                    <p className="eyebrow">{t("login.eyebrow")}</p>
                    <h1>{t("nav.signIn")}</h1>
                    <p>{t("login.description")}</p>
                </div>
                <section className="auth-card-content">
                    {query.has("error") && <p className="status-message error-message" role="alert">{t("login.error")}</p>}
                    {query.has("logout") && <p className="status-message">{t("login.logout")}</p>}
                    <form action="/login" method="post">
                        <input type="hidden" name="_csrf" value={csrfToken}/>
                        <label htmlFor="username">
                            {t("login.username")}
                            <input id="username" name="text" type="text" autoComplete="username"
                                   placeholder={t("login.usernamePlaceholder")} required/>
                        </label>
                        <label htmlFor="password">
                            {t("login.password")}
                            <input id="password" name="password" type="password" autoComplete="current-password"
                                   placeholder={t("login.passwordPlaceholder")} required/>
                        </label>
                        <button type="submit"><span>{t("nav.signIn")}</span>
                            <span className="button-arrow" aria-hidden="true">→</span></button>
                    </form>
                </section>
            </article>
        </main>
    </>;
}
