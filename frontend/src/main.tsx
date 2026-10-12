import {createRoot} from "react-dom/client";
import {useTheme} from "./hooks/useTheme";
import {useTranslations} from "./hooks/useTranslations";
import {HomePage} from "./pages/HomePage";
import {LoginPage} from "./pages/LoginPage";
import {ServerMessagePage} from "./pages/ServerMessagePage";
import {ServerPage} from "./pages/ServerPage";
import "./app.css";

function App() {
    const translations = useTranslations();
    const theme = useTheme();
    const pageProps = {...translations, ...theme};

    switch (new URLSearchParams(window.location.search).get("page")) {
        case "server":
            return <ServerPage {...pageProps}/>;
        case "server-message":
            return <ServerMessagePage language={translations.language} t={translations.t}/>;
        case "login":
            return <LoginPage {...pageProps}/>;
        default:
            return <HomePage {...pageProps}/>;
    }
}

createRoot(document.getElementById("root")!).render(<App/>);
