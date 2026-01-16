// ============ API ============
const API = {
    users: "/api/users",
    moviesSearch: "/api/movies/search",
    moviesImport: (externalId) => `/api/movies/import/${encodeURIComponent(externalId)}`,
    movieById: (id) => `/api/movies/${id}`,

    userLibrary: (userId) => `/api/users/${userId}/library`,
    userMovieStatus: (userId, userMovieId) => `/api/users/${userId}/library/${userMovieId}/status`,
    userMovieRating: (userId, userMovieId) => `/api/users/${userId}/library/${userMovieId}/rating`,
    userMovieLiked: (userId, userMovieId) => `/api/users/${userId}/library/${userMovieId}/liked`,
    userMovieDelete: (userId, userMovieId) => `/api/users/${userId}/library/${userMovieId}`,

    userWatchlists: (userId) => `/api/users/${userId}/watchlists`,
    watchlistById: (watchlistId) => `/api/watchlists/${watchlistId}`,
    watchlistDelete: (watchlistId) => `/api/watchlists/${watchlistId}`,
    watchlistAddItem: (watchlistId) => `/api/watchlists/${watchlistId}/items`,
    watchlistDeleteItem: (watchlistId, itemId) => `/api/watchlists/${watchlistId}/items/${itemId}`,
    watchlistReorder: (watchlistId) => `/api/watchlists/${watchlistId}/items/reorder`,
};

function $(id) { return document.getElementById(id); }

function getUserId() {
    const v = localStorage.getItem("userId");
    return v ? Number(v) : null;
}
function setUserId(userId) { localStorage.setItem("userId", String(userId)); }
function clearUserId() { localStorage.removeItem("userId"); }

function getWatchlistIdFromUrl() {
    const p = new URLSearchParams(window.location.search);
    const id = p.get("watchlistId");
    return id ? Number(id) : null;
}

async function apiFetch(url, options = {}) {
    const opts = { ...options };
    opts.headers = { "Content-Type": "application/json", ...(opts.headers || {}) };

    const res = await fetch(url, opts);
    if (res.status === 204) return null;

    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch (_) { data = text; }

    if (!res.ok) {
        const msg = (data && data.message) ? data.message : (typeof data === "string" ? data : "Request failed");
        const err = new Error(msg);
        err.status = res.status;
        err.data = data;
        throw err;
    }
    return data;
}

function requireUserOrRedirect() {
    const userId = getUserId();
    if (!userId) {
        window.location.href = "/index.html";
        return null;
    }
    return userId;
}

function showError(targetId, err) {
    const el = $(targetId);
    if (!el) return;
    el.innerText = `Ошибка: ${err.message} (HTTP ${err.status || "?"})`;
}

function clearError(targetId) {
    const el = $(targetId);
    if (!el) return;
    el.innerText = "";
}

function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
}

// ============ Toast ============
let toastTimer = null;
function toast(message, type = "ok") {
    let el = document.getElementById("toast");
    if (!el) {
        el = document.createElement("div");
        el.id = "toast";
        el.className = "toast";
        document.body.appendChild(el);
    }

    el.className = `toast ${type}`;
    el.textContent = message;

    requestAnimationFrame(() => el.classList.add("show"));

    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        el.classList.remove("show");
    }, 2000);
}

// ============ Modal (watchlists picker) ============
function ensureModal() {
    let b = document.getElementById("modalBackdrop");
    if (b) return b;

    b = document.createElement("div");
    b.id = "modalBackdrop";
    b.className = "modal-backdrop";
    b.innerHTML = `
    <div class="modal">
      <h3 id="modalTitle"></h3>
      <div id="modalBody"></div>
      <div class="actions">
        <button class="btn" id="modalCancel">Отмена</button>
        <button class="btn primary" id="modalOk">Добавить</button>
      </div>
    </div>
  `;
    document.body.appendChild(b);

    b.addEventListener("click", (e) => { if (e.target === b) hideModal(); });
    b.querySelector("#modalCancel").addEventListener("click", hideModal);

    return b;
}

function showModal({ title, bodyHtml, onOk }) {
    const b = ensureModal();
    b.querySelector("#modalTitle").textContent = title;
    b.querySelector("#modalBody").innerHTML = bodyHtml;
    b.classList.add("show");

    const okBtn = b.querySelector("#modalOk");
    okBtn.onclick = async () => { await onOk(b); };
}

function hideModal() {
    const b = document.getElementById("modalBackdrop");
    if (b) b.classList.remove("show");
}

async function openWatchlistsPicker({ userId, externalId, movieTitle }) {
    const lists = await apiFetch(API.userWatchlists(userId));

    if (!lists || lists.length === 0) {
        toast("У пользователя нет списков — сначала создай список", "warn");
        return;
    }

    const bodyHtml = `
    <div class="small" style="margin-bottom:8px;">
      Выбери один или несколько списков для: <b>${escapeHtml(movieTitle || externalId)}</b>
    </div>
    <div style="display:flex; flex-direction:column; gap:6px; max-height:260px; overflow:auto; padding-right:6px;">
      ${lists.map(w => `
        <label style="display:flex; align-items:center; gap:8px;">
          <input type="checkbox" data-wlid="${w.id}">
          <span>${escapeHtml(w.title)} <span class="small">(id=${w.id})</span></span>
        </label>
      `).join("")}
    </div>
  `;

    showModal({
        title: "Добавить в watchlists",
        bodyHtml,
        onOk: async (modal) => {
            const checked = Array.from(modal.querySelectorAll("input[type=checkbox][data-wlid]:checked"))
                .map(x => Number(x.getAttribute("data-wlid")))
                .filter(Boolean);

            if (checked.length === 0) {
                toast("Нужно выбрать хотя бы один список", "warn");
                return;
            }

            let okCount = 0;
            let alreadyCount = 0;

            for (const wlId of checked) {
                try {
                    await apiFetch(API.watchlistAddItem(wlId), {
                        method: "POST",
                        body: JSON.stringify({ externalId })
                    });
                    okCount++;
                } catch (e) {
                    // если фильм уже в списке — ожидаем 409 Conflict
                    if (e.status === 409) {
                        alreadyCount++;
                        continue;
                    }
                    throw e;
                }
            }

            hideModal();
            if (okCount > 0) toast(`Добавлено в списки: ${okCount}`, "ok");
            if (alreadyCount > 0) toast(`Уже было в списках: ${alreadyCount}`, "warn");
        }
    });
}

// ============ Header / Navigation ============
document.addEventListener("DOMContentLoaded", () => {
    injectHeader();

    const page = document.body.getAttribute("data-page");
    if (page === "index") initIndex();
    if (page === "movies") initMovies();
    if (page === "planned") initPlanned();
    if (page === "watched") initWatched();
    if (page === "watchlists") initWatchlists();
    if (page === "watchlist") initWatchlist();
});

function injectHeader() {
    const userId = getUserId();
    const page = document.body.getAttribute("data-page");

    const header = document.createElement("div");
    header.innerHTML = `
    <div style="border-bottom:1px solid #eee; padding:10px 0; margin-bottom:16px;">
      <div class="container">
        <div class="row" style="align-items:center; justify-content:space-between;">
          <div class="row" style="align-items:center; gap:10px;">
            <b style="margin-right:8px;">Movie Tracker</b>
            <a href="/index.html">Главная</a>
            <span class="small">|</span>
            <a href="/movies.html">Поиск</a>
            <span class="small">|</span>
            <a href="/planned.html">Смотреть позже</a>
            <span class="small">|</span>
            <a href="/watched.html">Просмотренные фильмы</a>
            <span class="small">|</span>
            <a href="/watchlists.html">Списки</a>
          </div>
          <div class="row" style="align-items:center; gap:10px;">
            <span class="small">userId: <b>${userId ?? "-"}</b></span>
            ${page === "index" ? "" : `<button class="btn" id="btnSwitchUser">Сменить пользователя</button>`}
          </div>
        </div>
      </div>
    </div>
  `;
    document.body.insertBefore(header, document.body.firstChild);

    const btn = header.querySelector("#btnSwitchUser");
    if (btn) {
        btn.addEventListener("click", () => {
            clearUserId();
            window.location.href = "/index.html";
        });
    }

    if (page !== "index") {
        if (!userId) window.location.href = "/index.html";
    }
}

// ============ Helpers for library/search ============
function buildLibIndexByExternalId(libraryItems) {
    // externalId -> { userMovieId, status }
    const map = new Map();
    for (const um of (libraryItems || [])) {
        const ext = um.movie?.externalId;
        if (ext) map.set(String(ext), { userMovieId: um.id, status: um.status });
    }
    return map;
}

function applyLibraryButtonStyles({ btnPlanned, btnWatched, status }) {
    // обе кнопки "нейтральные" по умолчанию, выбранная подсвечивается
    btnPlanned.classList.remove("primary");
    btnWatched.classList.remove("primary");

    btnPlanned.disabled = false;
    btnWatched.disabled = false;

    if (status === "PLANNED") {
        btnPlanned.classList.add("primary");
        btnPlanned.disabled = true;
    }
    if (status === "WATCHED") {
        btnWatched.classList.add("primary");
        btnWatched.disabled = true;
    }
}

function sortUserMovies(items, sortBy, dir) {
    const mul = dir === "desc" ? -1 : 1;
    const safeStr = (v) => (v === null || v === undefined) ? "" : String(v);

    return [...items].sort((a, b) => {
        const ma = a.movie || {};
        const mb = b.movie || {};

        if (sortBy === "addedAt") return mul * safeStr(a.createdAt).localeCompare(safeStr(b.createdAt));
        if (sortBy === "title") return mul * safeStr(ma.title).localeCompare(safeStr(mb.title));
        if (sortBy === "year") return mul * ((ma.year ?? 0) - (mb.year ?? 0));
        if (sortBy === "rating") return mul * ((a.rating ?? 0) - (b.rating ?? 0));
        return 0;
    });
}

// ============ INDEX ============
async function initIndex() {
    clearError("err");
    await loadUsersToSelect();

    // только кнопка "Выбрать"
    $("btnContinue")?.addEventListener("click", () => {
        const userId = Number($("userSelect").value);
        if (!userId) return;
        setUserId(userId);
        window.location.href = "/movies.html";
    });

    $("createUserForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        clearError("err");

        const body = {
            email: $("email").value.trim(),
            username: $("username").value.trim(),
            password: $("password").value.trim(),
        };

        try {
            const created = await apiFetch(API.users, { method: "POST", body: JSON.stringify(body) });
            await loadUsersToSelect(created.id);
            toast("Пользователь создан", "ok");
        } catch (err) {
            showError("err", err);
        }
    });
}

async function loadUsersToSelect(selectUserId = null) {
    const list = await apiFetch(API.users);
    const sel = $("userSelect");
    if (!sel) return;

    sel.innerHTML = `<option value="">-- выбери пользователя --</option>`;
    for (const u of list) {
        const opt = document.createElement("option");
        opt.value = u.id;
        opt.textContent = `${u.id}: ${u.username} (${u.email})`;
        sel.appendChild(opt);
    }
    if (selectUserId) sel.value = String(selectUserId);
}

// ============ MOVIES (Search) ============
async function initMovies() {
    const userId = requireUserOrRedirect();
    if (!userId) return;

    // paging: size фиксированный (кратно 3)
    const size = 6;
    let currentQuery = null;
    let currentYear = null;
    let currentPage = 1;

    const btnMore = $("btnMore");
    if (btnMore) {
        btnMore.style.display = "none";
        btnMore.disabled = true;

        btnMore.addEventListener("click", async () => {
            if (!currentQuery) return;
            currentPage += 1;
            await runMovieSearch({ userId, query: currentQuery, year: currentYear, page: currentPage, size, append: true });
        });
    }

    $("moviesSearchForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        clearError("err");

        const q = $("q").value.trim();
        const yearVal = $("year") ? $("year").value.trim() : "";
        if (!q) return;

        currentQuery = q;
        currentYear = yearVal || null;
        currentPage = 1;

        if (btnMore) {
            btnMore.disabled = false;
            btnMore.style.display = "none"; // покажем только если будут результаты
        }

        await runMovieSearch({ userId, query: currentQuery, year: currentYear, page: currentPage, size, append: false });
    });
}

async function runMovieSearch({ userId, query, year, page, size, append }) {
    clearError("err");

    const [library, watchlists] = await Promise.all([
        apiFetch(API.userLibrary(userId)),
        apiFetch(API.userWatchlists(userId)),
    ]);
    const libIndex = buildLibIndexByExternalId(library);

    const params = new URLSearchParams();
    params.set("query", query);
    if (year) params.set("year", year);
    params.set("page", String(page));
    params.set("size", String(size));

    const results = await apiFetch(`${API.moviesSearch}?${params.toString()}`);

    renderMoviesSearchResults({ results, userId, watchlists, libIndex, append });

    const btnMore = $("btnMore");
    if (btnMore) {
        if (!results || results.length === 0) {
            if (!append) btnMore.style.display = "none";
            return;
        }
        // если пришло меньше size — вероятно, дальше пусто
        btnMore.style.display = results.length < size ? "none" : "block";
        btnMore.disabled = results.length < size;
    }
}

function renderMoviesSearchResults({ results, userId, watchlists, libIndex, append }) {
    const box = $("searchResults");
    if (!box) return;
    if (!append) box.innerHTML = "";

    if (!results || results.length === 0) {
        if (!append) box.innerHTML = `<div class="small">Ничего не найдено</div>`;
        return;
    }

    for (const m of results) {
        const lib = libIndex.get(String(m.externalId));
        const statusInLib = lib?.status || null;

        const div = document.createElement("div");
        div.className = "card";

        div.innerHTML = `
      <img class="poster" src="${escapeHtml(m.posterUrl || "")}" alt="">
      <h3>${escapeHtml(m.title)} (${escapeHtml(m.year)})</h3>
      <div class="small">${escapeHtml(m.overview || "")}</div>

      <div class="row" style="margin-top:10px; gap:8px;">
        <button class="btn" data-act="planned">Смотреть позже</button>
        <button class="btn" data-act="watched">Просмотрено</button>
        <button class="btn" data-act="addWatchlists">Добавить в списки</button>
      </div>

      <div class="small">externalId: ${escapeHtml(m.externalId)}</div>
    `;
        box.appendChild(div);

        const btnPlanned = div.querySelector('[data-act="planned"]');
        const btnWatched = div.querySelector('[data-act="watched"]');
        const btnAddWl = div.querySelector('[data-act="addWatchlists"]');

        applyLibraryButtonStyles({ btnPlanned, btnWatched, status: statusInLib });

        btnPlanned.addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.userLibrary(userId), {
                    method: "POST",
                    body: JSON.stringify({ externalId: m.externalId, status: "PLANNED" })
                });
                toast("Добавлено: Смотреть позже", "ok");
                // обновим карточки (MVP)
                await runMovieSearch({ userId, query: $("q").value.trim(), year: $("year")?.value.trim() || null, page: 1, size: 6, append: false });
            } catch (err) { showError("err", err); }
        });

        btnWatched.addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.userLibrary(userId), {
                    method: "POST",
                    body: JSON.stringify({ externalId: m.externalId, status: "WATCHED" })
                });
                toast("Добавлено: Просмотрено", "ok");
                await runMovieSearch({ userId, query: $("q").value.trim(), year: $("year")?.value.trim() || null, page: 1, size: 6, append: false });
            } catch (err) { showError("err", err); }
        });

        btnAddWl.addEventListener("click", async () => {
            clearError("err");
            try {
                await openWatchlistsPicker({ userId, externalId: m.externalId, movieTitle: m.title });
            } catch (err) { showError("err", err); }
        });
    }
}

// ============ PLANNED ============
async function initPlanned() {
    const userId = requireUserOrRedirect();
    if (!userId) return;

    $("pageTitle") && ($("pageTitle").innerText = `Смотреть позже (user #${userId})`);

    $("libSortBy")?.addEventListener("change", () => refreshPlanned(userId));
    $("libSortDir")?.addEventListener("change", () => refreshPlanned(userId));

    await refreshPlanned(userId);
}

async function refreshPlanned(userId) {
    clearError("err");
    try {
        const [items, watchlists] = await Promise.all([
            apiFetch(API.userLibrary(userId)),
            apiFetch(API.userWatchlists(userId)),
        ]);
        const planned = items.filter(x => x.status === "PLANNED");
        renderLibrarySection({
            targetId: "itemsList",
            items: planned,
            userId,
            watchlists,
            allowRatingLike: false
        });
    } catch (err) {
        showError("err", err);
    }
}

// ============ WATCHED ============
async function initWatched() {
    const userId = requireUserOrRedirect();
    if (!userId) return;

    $("pageTitle") && ($("pageTitle").innerText = `Просмотрено (user #${userId})`);

    $("libSortBy")?.addEventListener("change", () => refreshWatched(userId));
    $("libSortDir")?.addEventListener("change", () => refreshWatched(userId));

    await refreshWatched(userId);
}

async function refreshWatched(userId) {
    clearError("err");
    try {
        const [items, watchlists] = await Promise.all([
            apiFetch(API.userLibrary(userId)),
            apiFetch(API.userWatchlists(userId)),
        ]);
        const watched = items.filter(x => x.status === "WATCHED");
        renderLibrarySection({
            targetId: "itemsList",
            items: watched,
            userId,
            watchlists,
            allowRatingLike: true
        });
    } catch (err) {
        showError("err", err);
    }
}

// ============ Library renderer (used by Planned/Watched) ============
function renderLibrarySection({ targetId, items, userId, watchlists, allowRatingLike }) {
    const box = $(targetId);
    if (!box) return;

    const sortBy = $("libSortBy") ? $("libSortBy").value : "addedAt";
    const sortDir = $("libSortDir") ? $("libSortDir").value : "desc";

    const sorted = sortUserMovies(items || [], sortBy, sortDir);
    box.innerHTML = "";

    if (!sorted || sorted.length === 0) {
        box.innerHTML = `<div class="small">Пусто</div>`;
        return;
    }

    for (const um of sorted) {
        const movie = um.movie || {};
        const div = document.createElement("div");
        div.className = "card";

        div.innerHTML = `
      <img class="poster" src="${escapeHtml(movie.posterUrl || "")}" alt="">
      <h3>${escapeHtml(movie.title)} (${escapeHtml(movie.year)})</h3>
      <div class="small">${escapeHtml(movie.overview || "")}</div>

      <div class="row" style="margin-top:10px; gap:8px; flex-wrap:wrap;">
        <button class="btn" data-act="toPlanned">Смотреть позже</button>
        <button class="btn" data-act="toWatched">Просмотрено</button>

        ${allowRatingLike ? `
          <label>Rating:
            <input data-act="rating" type="number" min="1" max="10" value="${um.rating ?? ""}" style="width:80px;">
          </label>
          <label>Liked:
            <input data-act="liked" type="checkbox" ${um.liked ? "checked" : ""}>
          </label>
        ` : `
        `}

        <button class="btn" data-act="addWatchlists">Добавить в списки</button>
        <button class="btn danger" data-act="delete">Удалить</button>
      </div>

      <div class="small">userMovieId: ${um.id} | externalId: ${escapeHtml(movie.externalId)}</div>
    `;
        box.appendChild(div);

        const btnPlanned = div.querySelector('[data-act="toPlanned"]');
        const btnWatched = div.querySelector('[data-act="toWatched"]');

        applyLibraryButtonStyles({ btnPlanned, btnWatched, status: um.status });

        btnPlanned.addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.userMovieStatus(userId, um.id), {
                    method: "PATCH",
                    body: JSON.stringify({ status: "PLANNED" })
                });
                toast("Статус: Смотреть позже", "ok");
                // после смены статуса — уедет на другую страницу (логично обновить текущую)
                if (document.body.getAttribute("data-page") === "watched") await refreshWatched(userId);
                else await refreshPlanned(userId);
            } catch (err) { showError("err", err); }
        });

        btnWatched.addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.userMovieStatus(userId, um.id), {
                    method: "PATCH",
                    body: JSON.stringify({ status: "WATCHED" })
                });
                toast("Статус: Просмотрено", "ok");
                if (document.body.getAttribute("data-page") === "planned") await refreshPlanned(userId);
                else await refreshWatched(userId);
            } catch (err) { showError("err", err); }
        });

        if (allowRatingLike) {
            const ratingInput = div.querySelector('[data-act="rating"]');
            const likedInput = div.querySelector('[data-act="liked"]');

            ratingInput?.addEventListener("change", async (e) => {
                clearError("err");
                try {
                    const v = e.target.value ? Number(e.target.value) : null;
                    await apiFetch(API.userMovieRating(userId, um.id), {
                        method: "PATCH",
                        body: JSON.stringify({ rating: v })
                    });
                    toast("Оценка сохранена", "ok");
                    await refreshWatched(userId);
                } catch (err) { showError("err", err); }
            });

            likedInput?.addEventListener("change", async (e) => {
                clearError("err");
                try {
                    await apiFetch(API.userMovieLiked(userId, um.id), {
                        method: "PATCH",
                        body: JSON.stringify({ liked: e.target.checked })
                    });
                    toast("Лайк сохранён", "ok");
                    await refreshWatched(userId);
                } catch (err) { showError("err", err); }
            });
        }

        div.querySelector('[data-act="delete"]')?.addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.userMovieDelete(userId, um.id), { method: "DELETE" });
                toast("Удалено из библиотеки", "ok");
                if (document.body.getAttribute("data-page") === "watched") await refreshWatched(userId);
                else await refreshPlanned(userId);
            } catch (err) { showError("err", err); }
        });

        div.querySelector('[data-act="addWatchlists"]')?.addEventListener("click", async () => {
            clearError("err");
            try {
                if (!movie.externalId) throw new Error("У фильма нет externalId");
                await openWatchlistsPicker({ userId, externalId: movie.externalId, movieTitle: movie.title });
            } catch (err) { showError("err", err); }
        });
    }
}

// ============ WATCHLISTS ============
async function initWatchlists() {
    const userId = requireUserOrRedirect();
    if (!userId) return;

    $("watchlistsTitle") && ($("watchlistsTitle").innerText = `Списки пользователя #${userId}`);

    $("createWatchlistForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        clearError("err");

        const body = {
            title: $("wlTitle").value.trim(),
            description: $("wlDesc").value.trim(),
        };

        try {
            await apiFetch(API.userWatchlists(userId), { method: "POST", body: JSON.stringify(body) });
            $("wlTitle").value = "";
            $("wlDesc").value = "";
            toast("Список создан", "ok");
            await refreshWatchlists(userId);
        } catch (err) {
            showError("err", err);
        }
    });

    await refreshWatchlists(userId);
}

async function refreshWatchlists(userId) {
    clearError("err");
    try {
        const list = await apiFetch(API.userWatchlists(userId));
        renderWatchlists(list);
    } catch (err) {
        showError("err", err);
    }
}

function renderWatchlists(list) {
    const box = $("watchlistsList");
    if (!box) return;

    box.innerHTML = "";

    if (!list || list.length === 0) {
        box.innerHTML = `<div class="small">Списков пока нет</div>`;
        return;
    }

    for (const wl of list) {
        const div = document.createElement("div");
        div.className = "card";
        div.innerHTML = `
      <h3>${escapeHtml(wl.title)}</h3>
      <div class="small">${escapeHtml(wl.description || "")}</div>
      <div class="small">id: ${wl.id} | createdAt: ${escapeHtml(wl.createdAt)}</div>
      <div class="row" style="margin-top:10px; gap:8px;">
        <button class="btn primary" data-act="open">Открыть</button>
        <button class="btn danger" data-act="delete">Удалить</button>
      </div>
    `;
        box.appendChild(div);

        div.querySelector('[data-act="open"]').addEventListener("click", () => {
            window.location.href = `/watchlist.html?watchlistId=${wl.id}`;
        });

        div.querySelector('[data-act="delete"]').addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.watchlistDelete(wl.id), { method: "DELETE" });
                toast("Список удалён", "ok");
                await refreshWatchlists(getUserId());
            } catch (err) { showError("err", err); }
        });
    }
}

// ============ WATCHLIST DETAIL ============
async function initWatchlist() {
    const userId = requireUserOrRedirect();
    if (!userId) return;

    const watchlistId = getWatchlistIdFromUrl();
    if (!watchlistId) {
        window.location.href = "/watchlists.html";
        return;
    }

    $("btnBack")?.addEventListener("click", () => window.location.href = "/watchlists.html");

    // поиск внутри конкретного watchlist оставляем, но size убираем (фикс)
    $("watchlistSearchForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        clearError("err");

        const query = $("q").value.trim();
        const year = $("year") ? $("year").value.trim() : "";
        if (!query) return;

        try {
            const params = new URLSearchParams();
            params.set("query", query);
            if (year) params.set("year", year);
            params.set("page", "1");
            params.set("size", "6"); // кратно 3

            const results = await apiFetch(`${API.moviesSearch}?${params.toString()}`);
            renderWatchlistSearchResults(results, watchlistId);
        } catch (err) {
            showError("err", err);
        }
    });

    $("sortSelect")?.addEventListener("change", () => refreshWatchlist(watchlistId));
    $("sortDir")?.addEventListener("change", () => refreshWatchlist(watchlistId));

    await refreshWatchlist(watchlistId);

    $("btnSaveOrder")?.addEventListener("click", async () => {
        clearError("err");
        try {
            const ids = Array.from(document.querySelectorAll("[data-itemid]"))
                .map(el => Number(el.getAttribute("data-itemid")));
            await apiFetch(API.watchlistReorder(watchlistId), {
                method: "PATCH",
                body: JSON.stringify({ orderedItemIds: ids })
            });
            toast("Порядок сохранён", "ok");
            await refreshWatchlist(watchlistId);
        } catch (err) { showError("err", err); }
    });
}

async function refreshWatchlist(watchlistId) {
    clearError("err");
    const wl = await apiFetch(API.watchlistById(watchlistId));

    $("watchlistHeader") && ($("watchlistHeader").innerText = `${wl.title} (id=${wl.id})`);
    $("watchlistDesc") && ($("watchlistDesc").innerText = wl.description || "");

    let items = wl.items || [];

    const sortBy = $("sortSelect") ? $("sortSelect").value : "position";
    const dir = $("sortDir") ? $("sortDir").value : "asc";
    const mul = dir === "desc" ? -1 : 1;

    // сортировка ТОЛЬКО отображения
    items = [...items].sort((a, b) => {
        if (sortBy === "position") return mul * ((a.position ?? 0) - (b.position ?? 0));
        if (sortBy === "title") return mul * String(a.movie?.title ?? "").localeCompare(String(b.movie?.title ?? ""));
        if (sortBy === "year") return mul * ((a.movie?.year ?? 0) - (b.movie?.year ?? 0));
        if (sortBy === "addedAt") return mul * (String(a.addedAt ?? "").localeCompare(String(b.addedAt ?? "")));
        return 0;
    });

    renderWatchlistItems(items, watchlistId);
}

function renderWatchlistSearchResults(results, watchlistId) {
    const box = $("searchResults");
    if (!box) return;

    box.innerHTML = "";

    if (!results || results.length === 0) {
        box.innerHTML = `<div class="small">Ничего не найдено</div>`;
        return;
    }

    for (const m of results) {
        const div = document.createElement("div");
        div.className = "card";
        div.innerHTML = `
      <img class="poster" src="${escapeHtml(m.posterUrl || "")}" alt="">
      <h3>${escapeHtml(m.title)} (${escapeHtml(m.year)})</h3>
      <div class="small">${escapeHtml(m.overview || "")}</div>
      <div class="row" style="margin-top:10px;">
        <button class="btn primary" data-act="add">Добавить в этот список</button>
      </div>
      <div class="small">externalId: ${escapeHtml(m.externalId)}</div>
    `;
        box.appendChild(div);

        div.querySelector('[data-act="add"]').addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.watchlistAddItem(watchlistId), {
                    method: "POST",
                    body: JSON.stringify({ externalId: m.externalId })
                });
                toast("Добавлено в список", "ok");
                await refreshWatchlist(watchlistId);
            } catch (err) {
                if (err.status === 409) toast("Этот фильм уже есть в списке", "warn");
                else showError("err", err);
            }
        });
    }
}

function renderStatusList(items, userId, watchlists, status) {
    const sortBy = $("sortBy").value;
    const sortDir = $("sortDir").value;
    const sorted = sortUserMovies(items, sortBy, sortDir);

    const box = $("list");
    box.innerHTML = "";

    if (sorted.length === 0) {
        box.innerHTML = `<div class="small">Пусто</div>`;
        return;
    }

    const wlOptions = watchlists.map(w => `<option value="${w.id}">${escapeHtml(w.title)} (id=${w.id})</option>`).join("");

    for (const um of sorted) {
        const movie = um.movie || {};
        const div = document.createElement("div");
        div.className = "card";

        const canRateLike = (status === "WATCHED");

        div.innerHTML = `
      <img class="poster" src="${escapeHtml(movie.posterUrl || "")}" alt="">
      <h3>${escapeHtml(movie.title)} (${escapeHtml(movie.year)})</h3>
      <div class="small">${escapeHtml(movie.overview || "")}</div>

      <div class="row" style="margin-top:10px;">
        <button class="btn ${status === "PLANNED" ? "primary" : ""}" id="toPl-${um.id}">Смотреть позже</button>
        <button class="btn ${status === "WATCHED" ? "primary" : ""}" id="toWa-${um.id}">Просмотрено</button>
        <button class="btn danger" id="del-${um.id}">Delete</button>
      </div>

      <div class="row" style="margin-top:10px; ${canRateLike ? "" : "opacity:0.5;"}">
        <label>Rating:
          <input id="rt-${um.id}" type="number" min="1" max="10" value="${um.rating ?? ""}" style="width:80px;" ${canRateLike ? "" : "disabled"}>
        </label>

        <label>Liked:
          <input id="lk-${um.id}" type="checkbox" ${um.liked ? "checked" : ""} ${canRateLike ? "" : "disabled"}>
        </label>
      </div>

      <div class="row" style="margin-top:10px;">
        <select id="wl-${um.id}" style="min-width:240px;">
          <option value="">-- добавить в watchlist --</option>
          ${wlOptions}
        </select>
        <button class="btn" id="addwl-${um.id}">Add</button>
      </div>
    `;

        box.appendChild(div);

        div.querySelector(`#toPl-${um.id}`).addEventListener("click", async () => {
            try {
                await apiFetch(API.userMovieStatus(userId, um.id), {
                    method: "PATCH",
                    body: JSON.stringify({ status: "PLANNED" })
                });
                toast("Перенесено в Смотреть позже");
                await refreshStatusPage(userId, status);
            } catch (e) { toast(e.message || "Ошибка", "err"); }
        });

        div.querySelector(`#toWa-${um.id}`).addEventListener("click", async () => {
            try {
                await apiFetch(API.userMovieStatus(userId, um.id), {
                    method: "PATCH",
                    body: JSON.stringify({ status: "WATCHED" })
                });
                toast("Перенесено в Просмотренные");
                await refreshStatusPage(userId, status);
            } catch (e) { toast(e.message || "Ошибка", "err"); }
        });

        div.querySelector(`#del-${um.id}`).addEventListener("click", async () => {
            try {
                await apiFetch(API.userMovieDelete(userId, um.id), { method: "DELETE" });
                toast("Удалено");
                await refreshStatusPage(userId, status);
            } catch (e) { toast(e.message || "Ошибка", "err"); }
        });

        if (canRateLike) {
            div.querySelector(`#rt-${um.id}`).addEventListener("change", async (e) => {
                try {
                    const v = e.target.value ? Number(e.target.value) : null;
                    await apiFetch(API.userMovieRating(userId, um.id), {
                        method: "PATCH",
                        body: JSON.stringify({ rating: v })
                    });
                    toast("Оценка сохранена");
                } catch (e2) { toast(e2.message || "Ошибка", "err"); }
            });

            div.querySelector(`#lk-${um.id}`).addEventListener("change", async (e) => {
                try {
                    await apiFetch(API.userMovieLiked(userId, um.id), {
                        method: "PATCH",
                        body: JSON.stringify({ liked: e.target.checked })
                    });
                    toast("Лайк обновлен");
                } catch (e2) { toast(e2.message || "Ошибка", "err"); }
            });
        }

        div.querySelector(`#addwl-${um.id}`).addEventListener("click", async () => {
            try {
                const wlId = Number(div.querySelector(`#wl-${um.id}`).value);
                if (!wlId) throw new Error("Выбери watchlist");
                await apiFetch(API.watchlistAddItem(wlId), {
                    method: "POST",
                    body: JSON.stringify({ externalId: movie.externalId })
                });
                toast("Добавлено в список");
            } catch (e) { toast(e.message || "Ошибка", "err"); }
        });
    }
}


function renderWatchlistItems(items, watchlistId) {
    const box = $("items");
    if (!box) return;

    box.innerHTML = "";

    if (!items || items.length === 0) {
        box.innerHTML = `<div class="small">В списке пока нет фильмов</div>`;
        return;
    }

    for (const it of items) {
        const movie = it.movie || {};
        const div = document.createElement("div");
        div.className = "card";
        div.setAttribute("data-itemid", String(it.id)); // для save order
        div.innerHTML = `
      <div class="row" style="gap:12px;">
        <div style="width:180px;">
          <img class="poster" src="${escapeHtml(movie.posterUrl || "")}" alt="">
        </div>
        <div style="flex:1;">
          <h3>${escapeHtml(movie.title)} (${escapeHtml(movie.year)})</h3>
          <div class="small">position: ${escapeHtml(it.position)} | addedAt: ${escapeHtml(it.addedAt)}</div>
          <div class="row" style="margin-top:10px; gap:8px;">
            <button class="btn" data-act="up">▲</button>
            <button class="btn" data-act="down">▼</button>
            <button class="btn danger" data-act="del">Удалить из списка</button>
          </div>
        </div>
      </div>
    `;
        box.appendChild(div);

        div.querySelector('[data-act="del"]').addEventListener("click", async () => {
            clearError("err");
            try {
                await apiFetch(API.watchlistDeleteItem(watchlistId, it.id), { method: "DELETE" });
                toast("Удалено из списка", "ok");
                await refreshWatchlist(watchlistId);
            } catch (err) { showError("err", err); }
        });

        // local reorder (DOM only), save with "Save order"
        div.querySelector('[data-act="up"]').addEventListener("click", () => {
            const prev = div.previousElementSibling;
            if (prev) box.insertBefore(div, prev);
        });
        div.querySelector('[data-act="down"]').addEventListener("click", () => {
            const next = div.nextElementSibling;
            if (next) box.insertBefore(next, div);
        });
    }
}
