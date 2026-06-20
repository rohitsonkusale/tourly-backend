package com.tourly.messaging.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility to detect and mask contact information in chat messages.
 * Uses a normalization layer to catch obfuscation attempts like "9two8four"
 * or "john at gmail dot com".
 *
 * All mappings (digit words, symbol substitutions, social platforms, email domains)
 * are data-driven — nothing is hardcoded in pattern strings.
 */
public final class ContactMaskingUtil {

    private ContactMaskingUtil() {
        // Utility class — no instantiation
    }

    // ========================================
    // DATA MAPS — Single source of truth
    // ========================================

    /** Word-to-digit mappings (supports mixed like "9two8four") */
    private static final Map<String, String> WORD_TO_DIGIT = new LinkedHashMap<>();

    static {
        // English
        WORD_TO_DIGIT.put("zero", "0");
        WORD_TO_DIGIT.put("one", "1");
        WORD_TO_DIGIT.put("two", "2");
        WORD_TO_DIGIT.put("three", "3");
        WORD_TO_DIGIT.put("four", "4");
        WORD_TO_DIGIT.put("five", "5");
        WORD_TO_DIGIT.put("six", "6");
        WORD_TO_DIGIT.put("seven", "7");
        WORD_TO_DIGIT.put("eight", "8");
        WORD_TO_DIGIT.put("nine", "9");

        // Hindi (romanized) — common spellings
        WORD_TO_DIGIT.put("shunya", "0");
        WORD_TO_DIGIT.put("sunya", "0");
        WORD_TO_DIGIT.put("ek", "1");
        WORD_TO_DIGIT.put("do", "2");
        WORD_TO_DIGIT.put("teen", "3");
        WORD_TO_DIGIT.put("tin", "3");
        WORD_TO_DIGIT.put("chaar", "4");
        WORD_TO_DIGIT.put("char", "4");
        WORD_TO_DIGIT.put("paanch", "5");
        WORD_TO_DIGIT.put("panch", "5");
        WORD_TO_DIGIT.put("paach", "5");
        WORD_TO_DIGIT.put("chhe", "6");
        WORD_TO_DIGIT.put("chhah", "6");
        WORD_TO_DIGIT.put("che", "6");
        WORD_TO_DIGIT.put("chha", "6");
        WORD_TO_DIGIT.put("saat", "7");
        WORD_TO_DIGIT.put("sat", "7");
        WORD_TO_DIGIT.put("aath", "8");
        WORD_TO_DIGIT.put("aat", "8");
        WORD_TO_DIGIT.put("nau", "9");

        // Marathi (romanized) — common spellings
        WORD_TO_DIGIT.put("shoonya", "0");
        WORD_TO_DIGIT.put("don", "2");
        WORD_TO_DIGIT.put("doan", "2");
        WORD_TO_DIGIT.put("sahaa", "6");
        WORD_TO_DIGIT.put("saha", "6");
        WORD_TO_DIGIT.put("nav", "9");

        // Tamil (romanized)
        WORD_TO_DIGIT.put("ondru", "1");
        WORD_TO_DIGIT.put("onnu", "1");
        WORD_TO_DIGIT.put("rendu", "2");
        WORD_TO_DIGIT.put("randu", "2");
        WORD_TO_DIGIT.put("moonu", "3");
        WORD_TO_DIGIT.put("naalu", "4");
        WORD_TO_DIGIT.put("anju", "5");
        WORD_TO_DIGIT.put("aaru", "6");
        WORD_TO_DIGIT.put("yezhu", "7");
        WORD_TO_DIGIT.put("yelu", "7");
        WORD_TO_DIGIT.put("ettu", "8");
        WORD_TO_DIGIT.put("onpathu", "9");

        // Telugu (romanized)
        WORD_TO_DIGIT.put("okati", "1");
        WORD_TO_DIGIT.put("rendu", "2");
        WORD_TO_DIGIT.put("moodu", "3");
        WORD_TO_DIGIT.put("naalugu", "4");
        WORD_TO_DIGIT.put("aidu", "5");
        WORD_TO_DIGIT.put("aaru", "6");
        WORD_TO_DIGIT.put("yeedu", "7");
        WORD_TO_DIGIT.put("enimidi", "8");
        WORD_TO_DIGIT.put("tommidi", "9");

        // Kannada (romanized)
        WORD_TO_DIGIT.put("ondu", "1");
        WORD_TO_DIGIT.put("eradu", "2");
        WORD_TO_DIGIT.put("mooru", "3");
        WORD_TO_DIGIT.put("naalku", "4");
        WORD_TO_DIGIT.put("aidu", "5");
        WORD_TO_DIGIT.put("aaru", "6");
        WORD_TO_DIGIT.put("yelu", "7");
        WORD_TO_DIGIT.put("entu", "8");
        WORD_TO_DIGIT.put("ombattu", "9");

        // Bengali (romanized)
        WORD_TO_DIGIT.put("shunno", "0");
        WORD_TO_DIGIT.put("aek", "1");
        WORD_TO_DIGIT.put("dui", "2");
        WORD_TO_DIGIT.put("tiin", "3");
        WORD_TO_DIGIT.put("chaar", "4");
        WORD_TO_DIGIT.put("paach", "5");
        WORD_TO_DIGIT.put("chhoy", "6");
        WORD_TO_DIGIT.put("shaat", "7");
        WORD_TO_DIGIT.put("aath", "8");
        WORD_TO_DIGIT.put("noy", "9");

        // Gujarati (romanized)
        WORD_TO_DIGIT.put("meenek", "0");
        WORD_TO_DIGIT.put("bey", "2");
        WORD_TO_DIGIT.put("tran", "3");
        WORD_TO_DIGIT.put("pach", "5");
        WORD_TO_DIGIT.put("chah", "6");
        WORD_TO_DIGIT.put("aath", "8");
    }

    /** Common symbol substitutions people use to disguise emails */
    private static final Map<String, String> EMAIL_SYMBOL_WORDS = new LinkedHashMap<>();

    static {
        // English
        EMAIL_SYMBOL_WORDS.put("at the rate", "@");
        EMAIL_SYMBOL_WORDS.put("at the rate of", "@");
        EMAIL_SYMBOL_WORDS.put("attherate", "@");
        EMAIL_SYMBOL_WORDS.put("at", "@");
        EMAIL_SYMBOL_WORDS.put("dot", ".");
        EMAIL_SYMBOL_WORDS.put("period", ".");
        EMAIL_SYMBOL_WORDS.put("underscore", "_");
        EMAIL_SYMBOL_WORDS.put("hyphen", "-");
        EMAIL_SYMBOL_WORDS.put("dash", "-");

        // Hindi/Hinglish (romanized)
        EMAIL_SYMBOL_WORDS.put("at the rate ka sign", "@");
        EMAIL_SYMBOL_WORDS.put("at ka sign", "@");
        EMAIL_SYMBOL_WORDS.put("at ka nishan", "@");
    }

    /** Known email domain keywords (for detecting "gmail", "yahoo", etc. in obfuscated text) */
    private static final List<String> EMAIL_DOMAIN_KEYWORDS = List.of(
            "gmail", "yahoo", "hotmail", "outlook", "protonmail", "icloud",
            "rediffmail", "aol", "zoho", "yandex", "mail", "live",
            "msn", "gmx", "tutanota", "fastmail"
    );

    /** Social media platform names and abbreviations */
    private static final List<String> SOCIAL_PLATFORMS = List.of(
            "instagram", "insta", "ig",
            "whatsapp", "whats app", "wa",
            "telegram", "tg",
            "facebook", "fb",
            "snapchat", "snap",
            "twitter", "x",
            "signal",
            "viber",
            "wechat",
            "line",
            "linkedin",
            "pinterest",
            "tiktok", "tik tok",
            "discord",
            "reddit",
            "youtube", "yt",
            "threads"
    );

    /** Contact intent phrases — signals that a user is trying to share contact info */
    private static final List<String> CONTACT_INTENT_PHRASES = List.of(
            // English
            "call me", "text me", "dm me", "message me",
            "reach me", "contact me", "ping me", "hit me up",
            "add me", "follow me", "find me on", "my number is",
            "my id is", "my handle is", "my username is",
            "connect with me", "buzz me",

            // Hindi (romanized)
            "mujhe call karo", "call karo", "phone karo", "ring karo",
            "mujhe message karo", "message karo", "msg karo",
            "whatsapp karo", "mujhe whatsapp karo", "wp karo",
            "mera number", "mera no", "mera num", "mera phone number",
            "number hai", "number ye hai", "number yeh hai",
            "mera id hai", "meri id", "meri id hai",
            "mujhe add karo", "follow karo", "mujhe follow karo",
            "pe contact karo", "pe msg karo", "pe message karo",
            "sampark karo", "mujhe sampark karo",
            "number de raha", "number de rahi", "number bhej",
            "number deta hu", "number deti hu", "number nota karo",
            "bat karo", "baat karo",

            // Marathi (romanized)
            "mala call kara", "call kara", "phone kara",
            "mala message kara", "message kara",
            "maza number", "mazha number", "number aahe",
            "mala follow kara", "mala add kara",
            "sampark kara", "mala sampark kara",
            "whatsapp kara",

            // Tamil (romanized)
            "ennai call pannunga", "call pannunga", "phone pannunga",
            "message pannunga", "ennai contact pannunga",
            "en number", "enna number",
            "whatsapp pannunga",

            // Telugu (romanized)
            "naku call cheyandi", "call cheyandi", "phone cheyandi",
            "message cheyandi", "naku message cheyandi",
            "na number", "naa number",
            "whatsapp cheyandi",

            // Bengali (romanized)
            "amake call koro", "call koro", "phone koro",
            "message koro", "amake message koro",
            "amar number", "number ta holo",
            "whatsapp koro"
    );

    private static final String MASK = "******";

    /** Platform-specific link patterns (wa.me, t.me, discord.gg, etc.) */
    private static final List<String> PLATFORM_LINK_DOMAINS = List.of(
            "wa.me", "t.me", "discord.gg", "bit.ly", "tinyurl.com",
            "instagram.com", "facebook.com", "fb.com", "twitter.com",
            "linkedin.com", "snapchat.com", "tiktok.com", "pinterest.com",
            "reddit.com", "youtube.com", "youtu.be", "telegram.me",
            "signal.me", "viber.com", "line.me", "threads.net"
    );

    /** Contact-intent phrases combined with platform names — kept lean and explicit */
    private static final List<String> CONTACT_INTENT_WITH_PLATFORM = List.of(
            // "my [platform]" patterns
            "my instagram", "my insta", "my ig",
            "my whatsapp", "my whats app", "my wa",
            "my telegram", "my tg",
            "my facebook", "my fb",
            "my snapchat", "my snap",
            "my twitter", "my discord", "my linkedin",
            // Hindi equivalents
            "mera instagram", "mera insta", "meri insta id",
            "mera whatsapp", "mera telegram", "mera facebook",
            "mera snapchat", "mera twitter",
            "meri instagram id", "mera fb",
            // "find/follow me on [platform]"
            "find me on instagram", "find me on insta", "find me on whatsapp",
            "find me on telegram", "find me on facebook", "find me on snapchat",
            "follow me on instagram", "follow me on insta", "follow me on facebook",
            "follow me on twitter", "follow me on tiktok",
            "search me on instagram", "search me on telegram",
            "add me on whatsapp", "add me on snapchat", "add me on discord",
            // Hindi equivalents
            "mujhe follow karo instagram", "mujhe follow karo insta",
            "mala follow kara instagram"
    );

    /** Static common words — prevents false positives in username detection */
    private static final Set<String> COMMON_WORDS = new HashSet<>(Arrays.asList(
            // English common words
            "the", "and", "for", "are", "but", "not", "you", "all",
            "can", "her", "was", "this", "that", "with", "have", "from",
            "they", "been", "will", "more", "when", "some", "them",
            "than", "its", "over", "just", "also", "very", "well",
            "here", "there", "where", "how", "why", "what", "which",
            "about", "would", "make", "like", "time", "know", "take",
            "people", "into", "could", "other", "after", "most",
            "trip", "travel", "host", "plan", "book", "tour", "guide",
            "please", "thanks", "hello", "sure", "okay", "great",
            "photos", "pictures", "images", "stories", "reels",
            "content", "posts", "page", "account", "profile",
            "amazing", "beautiful", "wonderful", "awesome", "nice",
            "interested", "available", "possible", "definitely",
            "tomorrow", "today", "yesterday", "morning", "evening",
            "night", "afternoon", "weekend", "monday", "tuesday",
            "wednesday", "thursday", "friday", "saturday", "sunday",
            "january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november", "december",
            "hotel", "resort", "villa", "cottage", "homestay",
            "beach", "mountain", "temple", "museum", "market",
            "group", "family", "friends", "couple", "solo",
            "budget", "luxury", "premium", "standard", "basic",
            "breakfast", "lunch", "dinner", "food", "water",
            "train", "flight", "bus", "cab", "taxi", "auto",
            "check", "need", "want", "looking", "searching",
            // Hindi/Hinglish
            "hai", "hain", "kya", "karo", "karna", "karenge",
            "mujhe", "mera", "meri", "mere", "aap", "aapka", "tumhara",
            "bahut", "accha", "achha", "theek", "thik", "sahi",
            "haan", "nahi", "nahin", "abhi", "kal", "parso",
            "subah", "shaam", "raat", "dopahar",
            "kitne", "kitna", "kitni", "kab", "kahan", "kaise",
            "jana", "aana", "jaana", "aate", "jaate",
            "bhai", "didi", "sir", "madam", "ji",
            "dekho", "dekh", "batao", "bataiye", "bolo", "suniye",
            "milte", "milenge", "milna", "chahiye", "chahte",
            "paisa", "paise", "rupees", "rupaye", "cost",
            "log", "logon", "sab", "sabko", "yahan", "wahan",
            "bohot", "bahot", "zyada", "kam", "thoda", "jyada",
            "safar", "yatra", "ghumna", "ghumne", "jagah",
            // Marathi
            "aahe", "kara", "karu", "mala", "mazha", "tumcha",
            "chan", "bara", "hot", "zala", "hota", "asel",
            // Tamil
            "enna", "inga", "anga", "panna", "pannunga", "vaanga",
            "nalla", "romba", "theriyum", "pannalam"
    ));

    // ========================================
    // COMPILED PATTERNS (built dynamically from data)
    // ========================================

    // Phone: 7+ consecutive digits (after normalization)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(\\+?\\d{1,3}[\\-. ]?)?(\\(?\\d{2,4}\\)?[\\-. ]?)?\\d{3,5}[\\-. ]?\\d{3,5}",
            Pattern.MULTILINE
    );

    // Spaced digits: "9 8 7 6 5 4 3 2 1 0"
    private static final Pattern SPACED_DIGITS_PATTERN = Pattern.compile(
            "\\b(\\d\\s+){6,}\\d\\b"
    );

    // Standard email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}",
            Pattern.CASE_INSENSITIVE
    );

    // Social handle: @username
    private static final Pattern SOCIAL_HANDLE_PATTERN = Pattern.compile(
            "@[a-zA-Z0-9._]{3,30}",
            Pattern.MULTILINE
    );

    // URLs
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[^\\s]+|www\\.[^\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

    // Built dynamically: "spelled email" pattern — e.g., "john at gmail dot com"
    private static final Pattern EMAIL_SPELLED_PATTERN;

    // Built dynamically: social platform keyword + username — e.g., "insta: myhandle"
    private static final Pattern SOCIAL_KEYWORD_PATTERN;

    // Built dynamically: detects word-digit mixed phone — e.g., "9two8four5six7eight90"
    private static final Pattern WORD_DIGIT_MIX_PATTERN;

    // Built dynamically: contact intent phrases
    private static final Pattern CONTACT_INTENT_PATTERN;

    // Built dynamically: domain keyword mention — e.g., "my id on gmail is john123"
    private static final Pattern DOMAIN_KEYWORD_WITH_ID_PATTERN;

    // Built dynamically: platform-specific short links (wa.me, t.me, discord.gg, etc.)
    private static final Pattern PLATFORM_LINK_PATTERN;

    // Built dynamically: contact-intent + platform combo (e.g., "my instagram is", "find me on telegram")
    private static final Pattern CONTACT_INTENT_PLATFORM_PATTERN;

    static {
        // Build spelled email pattern: [word]+ (at|attherate...) [word]+ (dot|period) [word]+
        String atWords = EMAIL_SYMBOL_WORDS.entrySet().stream()
                .filter(e -> e.getValue().equals("@"))
                .map(e -> Pattern.quote(e.getKey()))
                .collect(Collectors.joining("|"));
        String dotWords = EMAIL_SYMBOL_WORDS.entrySet().stream()
                .filter(e -> e.getValue().equals("."))
                .map(e -> Pattern.quote(e.getKey()))
                .collect(Collectors.joining("|"));
        EMAIL_SPELLED_PATTERN = Pattern.compile(
                "\\b[a-zA-Z0-9._%+\\-]+\\s*(" + atWords + ")\\s*[a-zA-Z0-9.\\-]+\\s*(" + dotWords + ")\\s*[a-zA-Z]{2,}\\b",
                Pattern.CASE_INSENSITIVE
        );

        // Build social keyword pattern from platform list
        String platformAlternation = SOCIAL_PLATFORMS.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        SOCIAL_KEYWORD_PATTERN = Pattern.compile(
                "\\b(" + platformAlternation + ")[:\\s]+[a-zA-Z0-9._]{3,30}",
                Pattern.CASE_INSENSITIVE
        );

        // Build word-digit mix pattern: sequences where digits and spelled words create 7+ digit numbers
        // e.g., "9two8four5six7eight90" — we match tokens that are digit-word combos
        String digitWords = WORD_TO_DIGIT.keySet().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        // Match: sequences of (digit|word-digit)(with optional spaces/separators) forming 4+ tokens
        WORD_DIGIT_MIX_PATTERN = Pattern.compile(
                "(?:\\d|" + digitWords + ")(?:[\\s\\-._]*(?:\\d|" + digitWords + ")){6,}",
                Pattern.CASE_INSENSITIVE
        );

        // Build contact intent pattern
        String intentAlternation = CONTACT_INTENT_PHRASES.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        CONTACT_INTENT_PATTERN = Pattern.compile(
                "\\b(" + intentAlternation + ")\\b",
                Pattern.CASE_INSENSITIVE
        );

        // Build domain keyword with identifier pattern
        // e.g., "my gmail is john.doe123" or "on yahoo john_smith"
        String domainAlternation = EMAIL_DOMAIN_KEYWORDS.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        DOMAIN_KEYWORD_WITH_ID_PATTERN = Pattern.compile(
                "\\b(" + domainAlternation + ")\\s*(id|is|:)?\\s*[a-zA-Z0-9._]{3,}",
                Pattern.CASE_INSENSITIVE
        );

        // Build platform link pattern (wa.me/xxx, t.me/xxx, instagram.com/xxx, etc.)
        String platformLinkAlternation = PLATFORM_LINK_DOMAINS.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        PLATFORM_LINK_PATTERN = Pattern.compile(
                "(" + platformLinkAlternation + ")/[a-zA-Z0-9._+\\-/]+",
                Pattern.CASE_INSENSITIVE
        );

        // Build contact-intent + platform pattern
        String intentPlatformAlternation = CONTACT_INTENT_WITH_PLATFORM.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        CONTACT_INTENT_PLATFORM_PATTERN = Pattern.compile(
                "\\b(" + intentPlatformAlternation + ")\\b[^.!?\\n]{0,40}",
                Pattern.CASE_INSENSITIVE
        );
    }

    // ========================================
    // PUBLIC API
    // ========================================

    /**
     * Result of masking operation.
     */
    public static class MaskResult {
        private final String maskedContent;
        private final boolean wasMasked;
        private final String detectedType;

        public MaskResult(String maskedContent, boolean wasMasked) {
            this.maskedContent = maskedContent;
            this.wasMasked = wasMasked;
            this.detectedType = null;
        }

        public MaskResult(String maskedContent, boolean wasMasked, String detectedType) {
            this.maskedContent = maskedContent;
            this.wasMasked = wasMasked;
            this.detectedType = detectedType;
        }

        public String getMaskedContent() {
            return maskedContent;
        }

        public boolean wasMasked() {
            return wasMasked;
        }

        /** Returns the type of contact info detected (PHONE, EMAIL, SOCIAL, URL, PLATFORM_LINK, MIXED) or null */
        public String getDetectedType() {
            return detectedType;
        }
    }

    /**
     * Masks any detected contact information in the given message content.
     * Uses both direct pattern matching AND normalization-based detection.
     *
     * @param content the raw message content
     * @return MaskResult containing the (possibly masked) content and whether masking occurred
     */
    public static MaskResult maskContactInfo(String content) {
        if (content == null || content.isBlank()) {
            return new MaskResult(content, false);
        }

        String result = content;

        // === PHASE 1: Direct pattern matching (catches obvious attempts) ===
        result = PLATFORM_LINK_PATTERN.matcher(result).replaceAll(MASK);
        result = URL_PATTERN.matcher(result).replaceAll(MASK);
        result = EMAIL_PATTERN.matcher(result).replaceAll(MASK);
        // Only apply spelled-email pattern if a known domain keyword is present
        if (containsEmailDomainKeyword(result)) {
            result = EMAIL_SPELLED_PATTERN.matcher(result).replaceAll(MASK);
        }
        result = SOCIAL_KEYWORD_PATTERN.matcher(result).replaceAll(MASK);
        result = SOCIAL_HANDLE_PATTERN.matcher(result).replaceAll(MASK);
        result = CONTACT_INTENT_PLATFORM_PATTERN.matcher(result).replaceAll(MASK);
        result = DOMAIN_KEYWORD_WITH_ID_PATTERN.matcher(result).replaceAll(MASK);
        result = SPACED_DIGITS_PATTERN.matcher(result).replaceAll(MASK);
        result = PHONE_PATTERN.matcher(result).replaceAll(MASK);

        // === PHASE 2: Normalization-based detection (catches obfuscation) ===
        result = maskObfuscatedPhoneNumbers(result);
        result = maskObfuscatedEmails(result);
        result = maskObfuscatedSocialHandles(result);

        boolean wasMasked = !result.equals(content);
        String detectedType = wasMasked ? detectViolationType(content) : null;
        return new MaskResult(result, wasMasked, detectedType);
    }

    /**
     * Checks whether the content contains any contact information without modifying it.
     */
    public static boolean containsContactInfo(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        // Direct pattern check
        if (PHONE_PATTERN.matcher(content).find()
                || EMAIL_PATTERN.matcher(content).find()
                || SOCIAL_HANDLE_PATTERN.matcher(content).find()
                || URL_PATTERN.matcher(content).find()
                || PLATFORM_LINK_PATTERN.matcher(content).find()
                || SOCIAL_KEYWORD_PATTERN.matcher(content).find()
                || CONTACT_INTENT_PLATFORM_PATTERN.matcher(content).find()
                || SPACED_DIGITS_PATTERN.matcher(content).find()
                || DOMAIN_KEYWORD_WITH_ID_PATTERN.matcher(content).find()) {
            return true;
        }

        // Spelled email — only check if domain keyword is present
        if (containsEmailDomainKeyword(content)
                && EMAIL_SPELLED_PATTERN.matcher(content).find()) {
            return true;
        }

        // Normalization check
        String normalized = normalizeDigitWords(content);
        if (PHONE_PATTERN.matcher(normalized).find()) {
            return true;
        }

        String emailNormalized = normalizeEmailSymbols(content);
        if (EMAIL_PATTERN.matcher(emailNormalized).find()) {
            return true;
        }

        return WORD_DIGIT_MIX_PATTERN.matcher(content).find();
    }

    // ========================================
    // PHASE 2: Obfuscation Detection
    // ========================================

    /**
     * Detects phone numbers where digits are spelled out or mixed.
     * e.g., "9two8four5six7eight90" → detected and masked.
     */
    private static String maskObfuscatedPhoneNumbers(String text) {
        Matcher matcher = WORD_DIGIT_MIX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group();
            String normalized = normalizeDigitWords(match);
            // Strip all non-digit characters after normalization
            String digitsOnly = normalized.replaceAll("[^0-9]", "");
            // If we get 7+ consecutive digits, it's likely a phone number
            if (digitsOnly.length() >= 7) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(MASK));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Detects emails where symbols are spelled out.
     * e.g., "john at gmail dot com" or "john attherate gmail period com"
     *
     * GUARD: Only triggers if a known email domain keyword is present in the text.
     * This prevents false positives like "meet me at the hotel dot on the corner".
     */
    private static String maskObfuscatedEmails(String text) {
        // GUARD: Only proceed if text contains a known email domain keyword
        if (!containsEmailDomainKeyword(text)) {
            return text;
        }

        // Already handled by EMAIL_SPELLED_PATTERN in Phase 1,
        // but this catches more creative variations using full normalization
        String normalized = normalizeEmailSymbols(text);

        // If the normalized text contains an email but the original didn't, mask the region
        if (!EMAIL_PATTERN.matcher(text).find() && EMAIL_PATTERN.matcher(normalized).find()) {
            return maskSegmentsContainingNormalizedEmail(text);
        }

        return text;
    }

    /**
     * Detects social media handles shared without the @ symbol.
     * e.g., "my insta id is travel_lover99" or "find me on telegram travel_lover99"
     * Also detects contact intent phrases like "call me", "dm me" combined with identifiers.
     */
    private static String maskObfuscatedSocialHandles(String text) {
        // If contact intent phrases are present with a following identifier, mask it
        Matcher intentMatcher = CONTACT_INTENT_PATTERN.matcher(text);
        while (intentMatcher.find()) {
            int afterIntent = intentMatcher.end();
            // Look for an identifier-like token within 30 chars after the intent phrase
            String remaining = text.substring(afterIntent, Math.min(text.length(), afterIntent + 40));
            Pattern idAfterIntent = Pattern.compile("\\s+(on\\s+)?([a-zA-Z0-9._]{3,30})\\b");
            Matcher idMatcher = idAfterIntent.matcher(remaining);
            if (idMatcher.find()) {
                String possibleHandle = idMatcher.group(2);
                if (looksLikeUsername(possibleHandle)) {
                    // Mask the entire region from intent phrase to end of handle
                    int maskStart = intentMatcher.start();
                    int maskEnd = afterIntent + idMatcher.end();
                    text = text.substring(0, maskStart) + MASK + text.substring(maskEnd);
                    // Re-run intent matcher on modified text
                    intentMatcher = CONTACT_INTENT_PATTERN.matcher(text);
                }
            }
        }

        // Check for platform mentions followed by identifiers
        for (String platform : SOCIAL_PLATFORMS) {
            Pattern loosePattern = Pattern.compile(
                    "\\b" + Pattern.quote(platform) + "\\b[^.!?\\n]{0,30}\\b([a-zA-Z0-9._]{3,30})\\b",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher looseMatcher = loosePattern.matcher(text);
            StringBuilder tempSb = new StringBuilder();
            boolean localFound = false;
            while (looseMatcher.find()) {
                // Only mask if the captured group looks like a username (not common words)
                String possibleHandle = looseMatcher.group(1);
                if (looksLikeUsername(possibleHandle)) {
                    looseMatcher.appendReplacement(tempSb, Matcher.quoteReplacement(MASK));
                    localFound = true;
                }
            }
            if (localFound) {
                looseMatcher.appendTail(tempSb);
                text = tempSb.toString();
            }
        }

        return text;
    }

    // ========================================
    // VIOLATION TYPE DETECTION
    // ========================================

    /**
     * Determines what type of contact info was detected in the original content.
     * Used for audit logging and violation tracking.
     */
    private static String detectViolationType(String content) {
        List<String> types = new ArrayList<>();

        if (URL_PATTERN.matcher(content).find() || PLATFORM_LINK_PATTERN.matcher(content).find()) {
            types.add("URL");
        }
        if (EMAIL_PATTERN.matcher(content).find()
                || (containsEmailDomainKeyword(content) && EMAIL_SPELLED_PATTERN.matcher(content).find())) {
            types.add("EMAIL");
        }
        if (PHONE_PATTERN.matcher(content).find()
                || SPACED_DIGITS_PATTERN.matcher(content).find()
                || WORD_DIGIT_MIX_PATTERN.matcher(content).find()) {
            types.add("PHONE");
        }
        if (SOCIAL_HANDLE_PATTERN.matcher(content).find()
                || SOCIAL_KEYWORD_PATTERN.matcher(content).find()
                || CONTACT_INTENT_PLATFORM_PATTERN.matcher(content).find()) {
            types.add("SOCIAL");
        }

        if (types.isEmpty()) return "OBFUSCATED";
        if (types.size() == 1) return types.get(0);
        return "MIXED";
    }

    // ========================================
    // NORMALIZATION HELPERS
    // ========================================

    /**
     * Checks if the text contains any known email domain keyword (gmail, yahoo, etc.).
     * Used as a guard to prevent false positives on "at" and "dot" in normal sentences.
     */
    private static boolean containsEmailDomainKeyword(String text) {
        String lower = text.toLowerCase();
        return EMAIL_DOMAIN_KEYWORDS.stream().anyMatch(lower::contains);
    }

    /**
     * Replaces spelled-out digit words with actual digits.
     * "9two8four" → "9284"
     * "nine eight seven six" → "9876"
     */
    private static String normalizeDigitWords(String text) {
        String result = text.toLowerCase();
        for (Map.Entry<String, String> entry : WORD_TO_DIGIT.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Replaces spelled-out email symbols with actual symbols.
     * "john at gmail dot com" → "john@gmail.com"
     */
    private static String normalizeEmailSymbols(String text) {
        String result = text.toLowerCase();
        // Apply longer phrases first to avoid partial matches
        List<Map.Entry<String, String>> sorted = new ArrayList<>(EMAIL_SYMBOL_WORDS.entrySet());
        sorted.sort((a, b) -> b.getKey().length() - a.getKey().length());
        for (Map.Entry<String, String> entry : sorted) {
            // Replace with word boundaries (spaces around the word)
            result = result.replaceAll(
                    "\\s+" + Pattern.quote(entry.getKey()) + "\\s+",
                    entry.getValue()
            );
        }
        // Remove remaining spaces around @ and .
        result = result.replaceAll("\\s*@\\s*", "@");
        result = result.replaceAll("\\s*\\.\\s*", ".");
        return result;
    }

    /**
     * Finds segments in original text that when normalized produce an email,
     * and masks those segments.
     */
    private static String maskSegmentsContainingNormalizedEmail(String text) {
        // Split into segments by sentence boundaries or chunks
        // Try to find the region that contains email-like keywords
        String lower = text.toLowerCase();

        // Look for patterns like "[word] at/attherate [word] dot [word]"
        for (Map.Entry<String, String> atEntry : EMAIL_SYMBOL_WORDS.entrySet()) {
            if (!atEntry.getValue().equals("@")) continue;

            int atIdx = lower.indexOf(atEntry.getKey());
            if (atIdx < 0) continue;

            // Found an "at" word — check if there's a dot word after it
            for (Map.Entry<String, String> dotEntry : EMAIL_SYMBOL_WORDS.entrySet()) {
                if (!dotEntry.getValue().equals(".")) continue;

                int dotIdx = lower.indexOf(dotEntry.getKey(), atIdx + atEntry.getKey().length());
                if (dotIdx < 0) continue;

                // Find the bounds: word before "at" and word after "dot"
                int start = findWordStartBefore(text, atIdx);
                int end = findWordEndAfter(text, dotIdx + dotEntry.getKey().length());

                if (start >= 0 && end > start) {
                    String before = text.substring(0, start);
                    String after = text.substring(end);
                    return before + MASK + after;
                }
            }
        }

        return text;
    }

    /** Finds the start of a word/token before the given index. */
    private static int findWordStartBefore(String text, int beforeIdx) {
        int i = beforeIdx - 1;
        // Skip whitespace
        while (i >= 0 && Character.isWhitespace(text.charAt(i))) i--;
        // Find word start
        while (i >= 0 && !Character.isWhitespace(text.charAt(i))) i--;
        return i + 1;
    }

    /** Finds the end of a word/token after the given index. */
    private static int findWordEndAfter(String text, int afterIdx) {
        int i = afterIdx;
        // Skip whitespace
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
        // Find word end
        while (i < text.length() && !Character.isWhitespace(text.charAt(i))) i++;
        return i;
    }

    /**
     * Heuristic: does this string look like a username vs a common English word?
     * Usernames typically have digits, underscores, dots, or mixed case patterns.
     */
    private static boolean looksLikeUsername(String text) {
        if (text == null || text.length() < 3) return false;

        String lower = text.toLowerCase();
        if (COMMON_WORDS.contains(lower)) return false;

        // Has digits → likely a username
        if (text.chars().anyMatch(Character::isDigit)) return true;

        // Has underscores or dots → likely a username
        if (text.contains("_") || text.contains(".")) return true;

        // Length 10+ with no spaces and not a common word → likely a username
        if (text.length() >= 10 && !text.contains(" ")) return true;

        return false;
    }
}
