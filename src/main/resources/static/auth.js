const BASE_URL = '';

async function authenticatedFetch(url, options = {}) {
    const token = localStorage.getItem('accessToken');
    options.headers = {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.headers
    };

    let response = await fetch(url, options);

    if (response.status === 401 || response.status === 403) {
        const clone = response.clone();
        let isExpired = false;
        try {
            const text = await clone.text();
            if (text.includes("expired") || text.includes("token")) isExpired = true;
        } catch (e) {}

        if (isExpired || response.status === 401) {
            const refreshToken = localStorage.getItem('refreshToken');
            if (!refreshToken) { return response; } // Гость

            const refreshResponse = await fetch(`${BASE_URL}/auth/refresh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken: refreshToken })
            });

            if (refreshResponse.ok) {
                const data = await refreshResponse.json();
                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('refreshToken', data.refreshToken);
                options.headers['Authorization'] = `Bearer ${data.accessToken}`;
                response = await fetch(url, options);
            } else {
                localStorage.clear();
                window.location.href = 'login.html';
            }
        }
    }
    return response;
}

function isUserLoggedIn() {
    return localStorage.getItem('accessToken') !== null;
}

function apiLogout() {
    localStorage.clear();
    window.location.href = 'index.html';
}

function getUsernameFromToken() {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return null;

        const base64Url = parts[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');

        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const claims = JSON.parse(jsonPayload);
        return claims.sub;
    } catch (e) {
        console.error("Ошибка парсинга JWT токена:", e);
        return null;
    }
}

function globalRenderNavbar() {
    const section = document.getElementById('navAuthSection');
    if (!section) return;

    if (isUserLoggedIn()) {
        const username = getUsernameFromToken() || "Пользователь";
        section.innerHTML = `
            <div class="d-flex align-items-center gap-3">
                <span class="navbar-text text-light fw-bold">
                    👤 <span class="text-info">${username}</span>
                </span>
                <button onclick="apiLogout()" class="btn btn-outline-danger btn-sm fw-bold">Выйти</button>
            </div>`;
    } else {
        section.innerHTML = `<a href="login.html" class="btn btn-success btn-sm fw-bold">Войти / Регистрация</a>`;
    }
}

if (!localStorage.getItem('accessToken') && !window.location.href.includes('login.html') && !window.location.href.endsWith('/') && !window.location.href.includes('index.html')) {
    window.location.href = 'login.html';
}

document.addEventListener("DOMContentLoaded", globalRenderNavbar);
