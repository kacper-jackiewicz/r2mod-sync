import {Header} from "../components/Header";
import {usePageSettings} from "../hooks/usePageSettings";
import type {PageProps} from "../types";

export function HomePage({language, setLanguage, t, theme, toggleTheme}: PageProps) {
    usePageSettings(language, "page.home.title", "", t);
    return <>
        <Header language={language} setLanguage={setLanguage} t={t} theme={theme} toggleTheme={toggleTheme}/>
        <main className="home-main">
            <section className="hero" aria-labelledby="hero-title">
                <div className="hero-copy">
                    <p className="eyebrow"><span className="eyebrow-dot" aria-hidden="true"></span>{t("home.eyebrow")}</p>
                    <h1 id="hero-title"><span className="hero-title-line">{t("home.titleFirst")}</span><br/>
                        <span className="hero-title-accent">{t("home.titleSecond")}</span></h1>
                    <p className="hero-description">{t("home.description")}</p>
                    <a className="button-link" href="/?page=login" role="button">
                        <span>{t("nav.signIn")}</span> <span className="button-arrow" aria-hidden="true">→</span>
                    </a>
                    <p className="hero-note">{t("home.note")}</p>
                </div>
                <div className="hero-art" aria-hidden="true">
                    <div className="orbit orbit-outer"></div>
                    <div className="orbit orbit-inner"></div>
                    <div className="sync-card">
                        <div className="sync-card-top">
                            <span className="sync-icon">↻</span>
                            <span className="sync-state"><span></span>{t("home.ready")}</span>
                        </div>
                        <div className="sync-lines"><span></span><span></span><span></span></div>
                        <div className="sync-card-bottom">
                            <span>{t("home.modLibrary")}</span><span className="sync-count">{t("home.inSync")}</span>
                        </div>
                    </div>
                    <span className="art-spark spark-one">✳</span>
                    <span className="art-spark spark-two">✳</span>
                </div>
            </section>
            <footer className="home-footer"><span>{t("home.footer")}</span></footer>
        </main>
    </>;
}
