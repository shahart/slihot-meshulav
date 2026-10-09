const jerusalemTimeZone = "Asia/Jerusalem";
const hebrewDateFormatter = new Intl.DateTimeFormat("en-u-ca-hebrew", {
  timeZone: jerusalemTimeZone,
  day: "numeric",
  month: "long",
  year: "numeric"
});
const weekdayFormatter = new Intl.DateTimeFormat("en", {
  timeZone: jerusalemTimeZone,
  weekday: "short"
});
const hourFormatter = new Intl.DateTimeFormat("en", {
  timeZone: jerusalemTimeZone,
  hour: "numeric",
  hourCycle: "h23"
});
const hebrewDateDisplayFormatter = new Intl.DateTimeFormat("he-IL-u-ca-hebrew", {
  timeZone: jerusalemTimeZone,
  day: "numeric",
  month: "long",
  year: "numeric"
});

function hebrewDate(date) {
  const parts = Object.fromEntries(
    hebrewDateFormatter.formatToParts(date)
      .filter(({ type }) => type !== "literal")
      .map(({ type, value }) => [type, value])
  );
  return { day: Number(parts.day), month: parts.month, year: Number(parts.year) };
}

function addDays(date, days) {
  const result = new Date(date);
  result.setUTCDate(result.getUTCDate() + days);
  return result;
}

/**
 * From 20:00 Jerusalem time onwards the evening service already follows the
 * upcoming day's selichot content.
 */
function effectiveServiceDate(today) {
  const hour = Number(hourFormatter.format(today));
  return hour >= 20 ? addDays(today, 1) : today;
}

function roshHashanaNear(date) {
  // The relevant period is from at most seven days before Rosh Hashanah
  // through 9 Tishri, eight days after it.
  for (let daysAway = -8; daysAway <= 7; daysAway += 1) {
    const candidate = addDays(date, daysAway);
    const dateInHebrew = hebrewDate(candidate);
    if (dateInHebrew.month === "Tishri" && dateInHebrew.day === 1) return candidate;
  }
  return null;
}

function shabbatBefore(roshHashana) {
  for (let daysBefore = 1; daysBefore <= 7; daysBefore += 1) {
    const candidate = addDays(roshHashana, -daysBefore);
    if (weekdayFormatter.format(candidate) === "Sat") return candidate;
  }
  throw new Error("לא ניתן לחשב את השבת שלפני ראש השנה");
}

function isValidSlichotDate(today) {
  const roshHashana = roshHashanaNear(today);
  if (!roshHashana) return false;

  const startOn = shabbatBefore(roshHashana);
  const endOn = addDays(roshHashana, 8); // 9 Tishri
  const dateInHebrew = hebrewDate(today);
  const isRoshHashana = dateInHebrew.month === "Tishri" &&
    (dateInHebrew.day === 1 || dateInHebrew.day === 2);
  return today >= startOn && today <= endOn && !isRoshHashana;
}

/**
 * Return the content file to insert between התחלה and המשך.
 *
 * @param {Date} today A Gregorian date; injectable so callers and tests can
 *   evaluate a particular calendar day. Defaults to the current instant.
 * @returns {string | null} A content filename, or null outside the permitted
 *   selichot dates.
 */
export function middleFile(today = new Date()) {
  console.info("SLIHOT_DEBUG_DATE=", today.toISOString());
  const serviceDate = effectiveServiceDate(today);
  if (!isValidSlichotDate(serviceDate)) return null;

  const dateInHebrew = hebrewDate(serviceDate);
  console.info("dateInHebrew=", dateInHebrew);

  if (dateInHebrew.month === "Elul" && dateInHebrew.day === 29) return "ערב ראש השנה עמוד 31";
  if (dateInHebrew.month === "Elul") {
    const roshHashana = roshHashanaNear(serviceDate);
    const firstDay = shabbatBefore(roshHashana);
    const daysSinceShabbat = Math.round((serviceDate - firstDay) / 86_400_000);
    if (daysSinceShabbat == 1) return "יום ראשון עמוד 24";
    else if (daysSinceShabbat == 2) return "יום שני עמוד 25";
    else if (daysSinceShabbat == 3) return "יום שלישי עמוד 26";
    else if (daysSinceShabbat == 4) return "יום רביעי עמוד 27";
    else if (daysSinceShabbat == 5) return "יום חמישי עמוד 28";
    else if (daysSinceShabbat == 6) return "יום שישי עמוד 29";
    else if (daysSinceShabbat == 7) return "יום שביעי עמוד 30";
  }
  if (dateInHebrew.month === "Tishri" && dateInHebrew.day === 3) {
    return weekdayFormatter.format(serviceDate) === "Sat" ? null : "צום גדליה עמוד 34";
  }
  if (dateInHebrew.month === "Tishri" && dateInHebrew.day === 9) return "ערב יום כיפור עמוד 44";
  if (dateInHebrew.month === "Tishri" && dateInHebrew.day >= 4 && dateInHebrew.day <= 8) {
    if (weekdayFormatter.format(serviceDate) === "Sat") return null;

    const tishri3 = addDays(serviceDate, 3 - dateInHebrew.day);
    const gedaliahWasPostponed = weekdayFormatter.format(tishri3) === "Sat";
    if (gedaliahWasPostponed && dateInHebrew.day === 4) return "צום גדליה עמוד 34";

    let selichotDay = 0;
    const firstAseretDay = gedaliahWasPostponed ? 5 : 4;
    for (let hebrewDay = firstAseretDay; hebrewDay <= dateInHebrew.day; hebrewDay += 1) {
      const candidate = addDays(serviceDate, hebrewDay - dateInHebrew.day);
      if (weekdayFormatter.format(candidate) !== "Sat") selichotDay += 1;
    }

    const aseretYemeiFiles = [
      "יום שני של עשרת ימי תשובה עמוד 36",
      "יום שלישי של עשרת ימי תשובה עמוד 38",
      "יום רביעי של עשרת ימי תשובה עמוד 40",
      "יום חמישי של עשרת ימי תשובה עמוד 42"
    ];
    return aseretYemeiFiles[selichotDay - 1] ?? null;
  }
  return "יום ראשון עמוד 24";
}

const contentDirectory = "./";
const contentFiles = [
  "התחלה", 
  "יום ראשון עמוד 24",
  "יום שני עמוד 25",
  "יום שלישי עמוד 26", 
  "יום רביעי עמוד 27", 
  "יום חמישי עמוד 28",
  "יום שישי עמוד 29", 
  "יום שביעי עמוד 30", 
  "ערב ראש השנה עמוד 31", 
  "צום גדליה עמוד 34",
  "יום שני של עשרת ימי תשובה עמוד 36",
  "יום שלישי של עשרת ימי תשובה עמוד 38",
  "יום רביעי של עשרת ימי תשובה עמוד 40",
  "יום חמישי של עשרת ימי תשובה עמוד 42",
  "ערב יום כיפור עמוד 44", "המשך"
];
const allowedFiles = new Set(contentFiles);

function escapeHtml(text) {
  return text.replace(/[&<>\"]/g, character => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" })[character]);
}

function renderInline(text) {
  return escapeHtml(text)
    .replace(/\[([^\]]+)\]\(((?:https?:\/\/|mailto:)[^\s)]+)\)/gi, '<a href="$2" rel="noopener noreferrer">$1</a>')
    .replace(/(\*\*|__)(?=\S)(.+?\S)\1/g, "<strong>$2</strong>")
    .replace(/\*(?=\S)(.+?\S)\*/g, "<em>$1</em>")
    .replace(/(^|[^\w])_(?=\S)(.+?\S)_($|[^\w])/g, "$1<em>$2</em>$3");
}

export function renderMarkdown(markdown) {
  return markdown.trim().split(/\n\s*\n/).map(block => {
    const text = block.trim();
    if (text === ">עמוד<") return '<div class="page-marker">עמוד</div>';
    const lines = text.split("\n");
    const unorderedItems = lines.map(line => line.match(/^\s*[-*+]\s+(.+)$/));
    if (unorderedItems.every(Boolean)) {
      return `<ul>${unorderedItems.map(item => `<li>${renderInline(item[1])}</li>`).join("")}</ul>`;
    }
    const orderedItems = lines.map(line => line.match(/^\s*\d+[.)]\s+(.+)$/));
    if (orderedItems.every(Boolean)) {
      return `<ol>${orderedItems.map(item => `<li>${renderInline(item[1])}</li>`).join("")}</ol>`;
    }
    const heading = text.match(/^(#{1,6})\s+(.+)$/);
    if (heading) {
      const level = heading[1].length;
      return `<h${level}>${renderInline(heading[2])}</h${level}>`;
    }
    const isVerse = /^\s{2,}/m.test(block);
    return `<p class="${isVerse ? "verse" : ""}">${renderInline(text)}</p>`;
  }).join("\n");
}

function renderDocument(markdownFiles) {
  const title = "<h1>סדר סליחות - נוסח משולב</h1>";
  return title + markdownFiles.map(renderMarkdown).join("\n") +
    '<div class="return-to-top"><button class="return-to-top-button" type="button">חזרה לראש העמוד</button></div>';
}

async function loadFile(name) {
  if (!allowedFiles.has(name)) throw new Error("שם קובץ תוכן לא תקין");
  const response = await fetch(contentDirectory + name + ".md");
  if (!response.ok) throw new Error(`לא ניתן לטעון את ${name}`);
  return response.text();
}

async function renderService(today = new Date()) {
  const middle = middleFile(today);
  document.querySelector("#current-middle").textContent = middle
    ? middle.replace(/\s*עמוד[\s\S]*$/, "").trim()
    : "";
  if (middle === null) {
    document.querySelector("#content").textContent = "לא אומרים סליחות היום";
    return;
  }

  console.info("middleFile=", middle);
  const files = ["התחלה", middle, "המשך"];
  const markdown = await Promise.all(files.map(loadFile));
  document.querySelector("#content").innerHTML = renderDocument(markdown);
}

async function showFile(name) {
  const markdown = await loadFile(name);
  document.querySelector("#content").innerHTML = renderDocument([markdown]);
}

function showError(error) {
  document.querySelector("#content").innerHTML = `<p class="error">${escapeHtml(error.message)}</p>`;
  console.error(error);
}

export function dateFromDebugFlag(value) {
  if (value === undefined || value === null || value === "") return null;
  const date = value instanceof Date ? new Date(value) : new Date(value);
  if (Number.isNaN(date.getTime())) {
    console.warn("SLIHOT_DEBUG_DATE must be a valid Gregorian date.");
    return null;
  }
  return date;
}

function configuredDate() {
  // Set ?SLIHOT_DEBUG_DATE=2026-09-05 in the URL to share a specific service
  // date. In Chrome DevTools, window.SLIHOT_DEBUG_DATE remains available for
  // ad-hoc debugging. The URL parameter takes precedence when both are set.
  const urlDate = new URLSearchParams(window.location.search).get("SLIHOT_DEBUG_DATE");
  return dateFromDebugFlag(urlDate) ?? dateFromDebugFlag(window.SLIHOT_DEBUG_DATE) ?? new Date();
}

function setUpContentMenu() {
  const dialog = document.querySelector("#content-menu");
  const list = document.querySelector("#content-file-list");
  const opener = document.querySelector("#content-menu-button");
  const dateHeading = document.querySelector("#content-menu-date");

  for (const name of contentFiles) {
    const item = document.createElement("li");
    const button = document.createElement("button");
    button.type = "button";
    button.className = "content-file-button";
    button.textContent = name;
    button.addEventListener("click", () => {
      dialog.close();
      showFile(name).catch(showError);
    });
    item.append(button);
    list.append(item);
  }

  opener.addEventListener("click", () => {
    dateHeading.textContent = hebrewDateDisplayFormatter.format(configuredDate());
    dialog.showModal();
  });
  document.querySelector("#content-menu-close").addEventListener("click", () => dialog.close());
  document.querySelector("#today-service-button").addEventListener("click", () => {
    dialog.close();
    renderService(configuredDate()).catch(showError);
  });
}

if (typeof document !== "undefined") {
  document.querySelector("#content").addEventListener("click", event => {
    if (event.target.closest(".return-to-top-button")) {
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  });
  setUpContentMenu();
  window.slihotDebug = {
    render() {
      return renderService(configuredDate()).catch(showError);
    }
  };
  window.slihotDebug.render();
}
