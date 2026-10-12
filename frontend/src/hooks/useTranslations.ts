import {useCallback, useEffect, useState} from "react";
import translations from "../translations.json";
import type {Language, Translate, TranslationKey} from "../types";

const languageStorageKey = "r2mod-sync-language";

function getInitialLanguage(): Language {
    return localStorage.getItem(languageStorageKey) === "pl" ? "pl" : "en";
}

export function useTranslations() {
    const [language, setLanguage] = useState<Language>(getInitialLanguage);
    const t = useCallback<Translate>(
        (key: TranslationKey) => translations[language][key] ?? key,
        [language]
    );

    useEffect(() => {
        document.documentElement.lang = language;
        localStorage.setItem(languageStorageKey, language);
    }, [language]);

    return {language, setLanguage, t};
}
