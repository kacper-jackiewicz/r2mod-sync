import type {Language, Translate} from "../types";

type HeaderProps = {
    language: Language;
    setLanguage: (language: Language) => void;
    t: Translate;
    theme: string;
    toggleTheme: () => void;
    signedIn?: boolean;
};

export function Header({language, setLanguage, t, theme, toggleTheme, signedIn = false}: HeaderProps) {
    const onLoginPage = new URLSearchParams(window.location.search).get("page") === "login";
    return <header className="site-header">
        <nav className="site-nav" aria-label={t("nav.mainNavigation")}>
            <a className="brand" href="/" aria-label={t("brand.home")}>
                <span className="brand-mark" aria-hidden="true">r2</span>
                <span>R2ModSync</span>
            </a>
            <div className="nav-actions">
                <label className="language-picker">
                    <span className="visually-hidden">{t("language.label")}</span>
                    <select className="language-select" aria-label={t("language.label")} value={language}
                            onChange={(event) => setLanguage(event.target.value as Language)}>
                        <option value="en">English</option>
                        <option value="pl">Polski</option>
                    </select>
                </label>
                <button type="button" className="theme-toggle secondary"
                        aria-label={t(theme === "dark" ? "theme.switchToLight" : "theme.switchToDark")}
                        onClick={toggleTheme}>
                    <span className="theme-toggle-icon" aria-hidden="true">{theme === "dark" ? "☀" : "☾"}</span>
                    <span id="theme-toggle-label">{t(theme === "dark" ? "theme.lightMode" : "theme.darkMode")}</span>
                </button>
                {signedIn
                    ? <span className="account-label"><span className="account-dot"></span>{t("nav.signedIn")}</span>
                    : onLoginPage
                        ? <a className="nav-link" href="/">{t("nav.home")}</a>
                        : <a className="nav-link" href="/?page=login">{t("nav.signIn")}</a>}
            </div>
        </nav>
    </header>;
}
