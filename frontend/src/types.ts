import translations from "./translations.json";

export type Language = "en" | "pl";
export type TranslationKey = keyof typeof translations.en;
export type Translate = (key: TranslationKey) => string;
export type PageProps = {
    language: Language;
    setLanguage: (language: Language) => void;
    t: Translate;
    theme: string;
    toggleTheme: () => void;
};

export type ServerStatus = "NOT_CONFIGURED" | "RUNNING" | "STOPPED" | "UPDATING" | "UNKNOWN" | "CHECKING";
export type OperationState = "PENDING" | "COMPLETED" | "FAILED";
export type OperationStatus = {state: OperationState; messageKey: string};
