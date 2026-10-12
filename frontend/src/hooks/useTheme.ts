import {useEffect, useState} from "react";

const themeStorageKey = "r2mod-sync-theme";

export function useTheme() {
    const [theme, setTheme] = useState(() => localStorage.getItem(themeStorageKey) === "dark" ? "dark" : "light");

    useEffect(() => {
        document.documentElement.dataset.theme = theme;
    }, [theme]);

    const toggleTheme = () => {
        const nextTheme = theme === "dark" ? "light" : "dark";
        localStorage.setItem(themeStorageKey, nextTheme);
        setTheme(nextTheme);
    };

    return {theme, toggleTheme};
}
