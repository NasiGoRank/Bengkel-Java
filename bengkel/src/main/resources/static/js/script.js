// Global state
let currentPage = 'dashboard';

console.log("JavaScript loaded successfully from /js/script.js");

// === AUTHENTICATION ===
async function doLogin() {
    const username = document.getElementById('login-user').value;
    const password = document.getElementById('login-pass').value;

    if (!username || !password) {
        alert('Username dan password harus diisi!');
        return;
    }

    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            document.getElementById('login-page').style.display = 'none';
            document.getElementById('main-app').style.display = 'block';
            loadDashboard();
            console.log('Login successful');
        } else {
            alert('Login gagal! Periksa username dan password.');
        }
    } catch (e) {
        console.error('Login error:', e);
        alert('Error saat login: ' + e.message);
    }
}

function logout() {
    if (confirm('Anda yakin ingin logout?')) {
        document.getElementById('main-app').style.display = 'none';
        document.getElementById('login-page').style.display = 'block';
        document.getElementById('login-pass').value = '';
        console.log('Logged out');
    }
}

// === NAVIGATION ===
function showPage(page) {
    // Update active sidebar
    document.querySelectorAll('.sidebar-item').forEach(item => item.classList.remove('active'));

    // Activate clicked item
    const items = document.querySelectorAll('.sidebar-item');
    const pageIndex = ['dashboard', 'customer', 'item', 'transaction', 'report'].indexOf(page);
    if (pageIndex >= 0 && items[pageIndex]) items[pageIndex].classList.add('active');

    // Hide all pages, show selected
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(`page-${page}`).classList.add('active');

    currentPage = page;

    // Load Data
    if (page === 'dashboard') loadDashboard();
    else if (page === 'customer') loadTable('customer');
    else if (page === 'item') loadTable('item');
    else if (page === 'transaction') loadTable('transaction');
    else if (page === 'report') {
        initReportDates();
        loadReport();
    }
    console.log('Showing page:', page);
}

// === DASHBOARD ===
async function loadDashboard() {
    try {
        const response = await fetch('/api/dashboard');
        const data = await response.json();

        document.getElementById('customer-count').textContent = data.customers || 0;
        document.getElementById('item-count').textContent = data.items || 0;
        document.getElementById('transaction-count').textContent = data.transactions || 0;
        document.getElementById('revenue-count').textContent = 'Rp ' + formatCurrency(data.revenue);

        // Load recent activity (ambil transaksi terakhir)
        const transResponse = await fetch('/api/transactions');
        const transactions = await transResponse.json();
        updateActivityTable(transactions);

    } catch (e) {
        console.error('Error loading dashboard:', e);
    }
}

function updateActivityTable(transactions) {
    const tbody = document.querySelector('#table-activity tbody');
    tbody.innerHTML = '';

    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state"><div class="empty-state-icon">📊</div><div>Tidak ada aktivitas</div></td></tr>`;
        return;
    }

    // Ambil 5 transaksi terakhir (backend sdh sort by date desc biasanya, tp kita pastikan reverse jk perlu)
    const latestTransactions = transactions.slice(0, 5);

    latestTransactions.forEach(t => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${formatDate(t.tanggal)}</td>
            <td><span class="badge badge-success">Transaksi</span></td>
            <td>${escapeHtml(t.customerName)} - ${escapeHtml(t.keluhan || 'Tidak ada keluhan')}</td>
            <td><span class="badge badge-success">Selesai</span></td>
        `;
        tbody.appendChild(row);
    });
}

// === TABLE & DATA LOADING ===
async function loadTable(type) {
    const endpoint = `/api/${type}s`; // customers, items, transactions
    try {
        const response = await fetch(endpoint);
        const data = await response.json();
        renderTable(type, data);
    } catch (e) {
        console.error(`Error loading ${type}:`, e);
    }
}

function renderTable(type, data) {
    const tableId = `table-${type}`;
    const tbody = document.querySelector(`#${tableId} tbody`);
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!data || data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><div class="empty-state-icon">📄</div><div>Tidak ada data</div></td></tr>`;
        return;
    }

    data.forEach(item => {
        const row = document.createElement('tr');

        if (type === 'customer') {
            const statusBadge = Math.random() > 0.3 ? 'badge-success' : 'badge-danger'; // Simulasi status
            row.innerHTML = `
                <td><small>${item.id.substring(0, 8)}...</small></td>
                <td>${escapeHtml(item.nama)}</td>
                <td>${escapeHtml(item.noTelp)}</td>
                <td>${escapeHtml(item.alamat)}</td>
                <td><span class="badge ${statusBadge}">Aktif</span></td>
                <td>
                    <button class="btn" onclick="openCustomerModal('${item.id}', '${item.nama}', '${item.noTelp}', '${item.alamat}')">Edit</button>
                    <button class="btn btn-danger" onclick="deleteItemAPI('customers', '${item.id}')">Hapus</button>
                </td>
            `;
        } else if (type === 'item') {
            const status = item.stok > 0 ? 'Tersedia' : 'Habis';
            const badge = item.stok > 10 ? 'badge-success' : (item.stok > 0 ? 'badge-warning' : 'badge-danger');
            row.innerHTML = `
                <td><small>${item.id.substring(0, 8)}...</small></td>
                <td>${escapeHtml(item.namaBarang)}</td>
                <td>${item.stok}</td>
                <td>Rp ${formatCurrency(item.harga)}</td>
                <td><span class="badge ${badge}">${status}</span></td>
                <td>
                    <button class="btn" onclick="openItemModal('${item.id}', '${item.namaBarang}', ${item.stok}, ${item.harga})">Edit</button>
                    <button class="btn btn-danger" onclick="deleteItemAPI('items', '${item.id}')">Hapus</button>
                </td>
            `;
        } else if (type === 'transaction') {
            row.innerHTML = `
                <td><small>${item.id.substring(0, 8)}...</small></td>
                <td>${formatDate(item.tanggal)}</td>
                <td>${escapeHtml(item.customerName)}</td>
                <td>${escapeHtml(item.keluhan)}</td>
                <td>Rp ${formatCurrency(item.totalBayar)}</td>
                <td><span class="badge badge-success">Selesai</span></td>
                <td>
                    <button class="btn btn-danger" onclick="deleteItemAPI('transactions', '${item.id}')">Hapus</button>
                </td>
            `;
        } else if (type === 'report') {
            row.innerHTML = `
                <td>${formatDate(item.tanggal)}</td>
                <td><small>${item.id.substring(0, 8)}...</small></td>
                <td>${escapeHtml(item.customerName)}</td>
                <td>${escapeHtml(item.keluhan)}</td>
                <td>Rp ${formatCurrency(item.totalBayar)}</td>
                <td><span class="badge badge-success">Selesai</span></td>
            `;
        }
        tbody.appendChild(row);
    });
}

// === CRUD OPERATIONS ===

// Generic Delete
async function deleteItemAPI(endpoint, id) {
    if (!confirm('Yakin ingin menghapus data ini?')) return;

    try {
        const res = await fetch(`/api/${endpoint}/${id}`, { method: 'DELETE' });
        if (res.ok) {
            // Refresh current table
            if (endpoint.includes('customer')) loadTable('customer');
            if (endpoint.includes('item')) loadTable('item');
            if (endpoint.includes('transaction')) loadTable('transaction');
        } else {
            alert('Gagal menghapus data');
        }
    } catch (e) {
        console.error(e);
        alert('Error: ' + e.message);
    }
}

// Customer
function openCustomerModal(id = '', name = '', phone = '', address = '') {
    document.getElementById('customer-id').value = id;
    document.getElementById('customer-name').value = name || '';
    document.getElementById('customer-phone').value = phone || '';
    document.getElementById('customer-address').value = address || '';
    document.getElementById('modal-customer').style.display = 'flex';
}

async function saveCustomer() {
    const id = document.getElementById('customer-id').value;
    const customer = {
        id: id || null,
        nama: document.getElementById('customer-name').value,
        noTelp: document.getElementById('customer-phone').value,
        alamat: document.getElementById('customer-address').value
    };

    await fetch('/api/customers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customer)
    });

    closeModal('modal-customer');
    loadTable('customer');
}

async function searchCustomer() {
    const query = document.getElementById('search-customer').value;
    const res = await fetch(`/api/customers?query=${query}`);
    const data = await res.json();
    renderTable('customer', data);
}

// Item
function openItemModal(id = '', name = '', stock = '', price = '') {
    document.getElementById('item-id').value = id;
    document.getElementById('item-name').value = name || '';
    document.getElementById('item-stock').value = stock || '';
    document.getElementById('item-price').value = price || '';
    document.getElementById('modal-item').style.display = 'flex';
}

async function saveItem() {
    const id = document.getElementById('item-id').value;
    const item = {
        id: id || null,
        namaBarang: document.getElementById('item-name').value,
        stok: parseInt(document.getElementById('item-stock').value),
        harga: parseFloat(document.getElementById('item-price').value)
    };

    await fetch('/api/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(item)
    });

    closeModal('modal-item');
    loadTable('item');
}

async function searchItem() {
    // Note: Backend perlu support search item jika belum ada, sementara load all
    loadTable('item');
}

// Transaction
async function openTransactionModal() {
    // Load customers for dropdown
    const res = await fetch('/api/customers');
    const customers = await res.json();

    const select = document.getElementById('transaction-customer');
    select.innerHTML = '<option value="">Pilih Customer</option>';
    customers.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = `${c.nama} (${c.noTelp})`;
        select.appendChild(opt);
    });

    document.getElementById('transaction-id').value = '';
    document.getElementById('transaction-date').value = new Date().toISOString().split('T')[0];
    document.getElementById('transaction-complaint').value = '';
    document.getElementById('transaction-total').value = '';

    document.getElementById('modal-transaction').style.display = 'flex';
}

async function saveTransaction() {
    const trans = {
        id: document.getElementById('transaction-id').value || null,
        customer: { id: document.getElementById('transaction-customer').value }, // Object relation
        tanggal: document.getElementById('transaction-date').value,
        keluhan: document.getElementById('transaction-complaint').value,
        totalBayar: parseFloat(document.getElementById('transaction-total').value)
    };

    if (!trans.customer.id) { alert('Pilih customer!'); return; }

    await fetch('/api/transactions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(trans)
    });

    closeModal('modal-transaction');
    loadTable('transaction');
}

// Report
function initReportDates() {
    const today = new Date().toISOString().split('T')[0];
    const firstDay = new Date();
    firstDay.setDate(1);
    const startDateInput = document.getElementById('report-start-date');
    const endDateInput = document.getElementById('report-end-date');

    if (startDateInput && !startDateInput.value) startDateInput.value = firstDay.toISOString().split('T')[0];
    if (endDateInput && !endDateInput.value) endDateInput.value = today;
}

async function loadReport() {
    const start = document.getElementById('report-start-date').value;
    const end = document.getElementById('report-end-date').value;

    const res = await fetch(`/api/report?start=${start}&end=${end}`);
    const data = await res.json();
    renderTable('report', data);
}

function generatePDFReport() {
    const start = document.getElementById('report-start-date').value;
    const end = document.getElementById('report-end-date').value;
    window.open(`/api/report/pdf?start=${start}&end=${end}`, '_blank');
}

function printReport() {
    // Sama seperti sebelumnya, hanya ambil data dari DOM tabel report yg sedang tampil
    const table = document.getElementById('table-report');
    if (table.querySelector('tbody tr').innerText.includes('Tidak ada data')) {
        alert('Tampilkan data laporan terlebih dahulu');
        return;
    }

    const printWindow = window.open('', '_blank', 'width=800,height=600');
    // ... (Kode print HTML sama seperti sebelumnya, tidak perlu fetch baru)
    // Sederhananya kita clone tabel yang ada
    const tableClone = table.cloneNode(true);

    printWindow.document.write(`
        <html><head><title>Cetak Laporan</title>
        <style>table { width: 100%; border-collapse: collapse; } th, td { border: 1px solid black; padding: 8px; }</style>
        </head><body>
        <h2>Laporan Bengkel</h2>
        ${tableClone.outerHTML}
        <script>setTimeout(() => window.print(), 500);</script>
        </body></html>
    `);
}

// Utils
function closeModal(id) {
    document.getElementById(id).style.display = 'none';
    const form = document.querySelector(`#${id} form`);
    if (form) form.reset();
}

function formatCurrency(amount) {
    if (!amount) return '0';
    return amount.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

function formatDate(dateString) {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('id-ID', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch (e) { return dateString; }
}

function escapeHtml(text) {
    if (!text) return '';
    return text.toString().replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

// Initialize
window.onload = function () {
    initReportDates();
    if (document.getElementById('transaction-date'))
        document.getElementById('transaction-date').value = new Date().toISOString().split('T')[0];
};