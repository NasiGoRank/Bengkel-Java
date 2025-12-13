// Global state
let currentPage = 'dashboard';
// Menyimpan data master agar tidak fetch berulang kali saat kalkulasi di modal
let availableItems = [];
let availableServices = [];

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
    // Index mapping harus sesuai urutan di HTML sidebar
    const pageIndex = ['dashboard', 'customer', 'service', 'item', 'transaction', 'report'].indexOf(page);
    if (pageIndex >= 0 && items[pageIndex]) items[pageIndex].classList.add('active');

    // Hide all pages, show selected
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(`page-${page}`).classList.add('active');

    currentPage = page;

    // Load Data
    if (page === 'dashboard') loadDashboard();
    else if (page === 'customer') loadTable('customer');
    else if (page === 'service') loadTable('service');
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

        // Load recent activity
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
    // endpoint biasanya type + 's', e.g., customers, items
    const endpoint = `/api/${type}s`;
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
        let colSpan = 6; // default
        if (type === 'service') colSpan = 4;
        tbody.innerHTML = `<tr><td colspan="${colSpan}" class="empty-state"><div class="empty-state-icon">📄</div><div>Tidak ada data</div></td></tr>`;
        return;
    }

    data.forEach(item => {
        const row = document.createElement('tr');

        if (type === 'customer') {
            const statusBadge = Math.random() > 0.3 ? 'badge-success' : 'badge-danger';
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
        } else if (type === 'service') {
            row.innerHTML = `
                <td>${item.id}</td>
                <td>${escapeHtml(item.namaService)}</td>
                <td>Rp ${formatCurrency(item.harga)}</td>
                <td>
                    <button class="btn btn-danger" onclick="deleteItemAPI('services', '${item.id}')">Hapus</button>
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
            // Tentukan warna badge berdasarkan status
            let badgeColor = 'badge-success';
            if (item.status === 'Baru') badgeColor = 'badge-warning'; // Kuning
            if (item.status === 'Sedang Dikerjakan') badgeColor = 'badge-primary'; // Biru (perlu tambah CSS)
            if (item.status === 'Batal') badgeColor = 'badge-danger'; // Merah

            row.innerHTML = `
                <td><small>${item.id.substring(0, 8)}...</small></td>
                <td>${formatDate(item.tanggal)}</td>
                <td>${escapeHtml(item.customerName)}</td>
                <td>${escapeHtml(item.keluhan)}</td>
                <td>Rp ${formatCurrency(item.totalBayar)}</td>
                <td><span class="badge ${badgeColor}">${item.status || 'Selesai'}</span></td>
                <td>
                    <button class="btn" onclick="editTransaction('${item.id}')">Edit</button>
                    <button class="btn btn-danger" onclick="deleteItemAPI('transactions', '${item.id}')">Hapus</button>
                </td>
            `;
        } else if (type === 'report') {
            let badgeColor = 'badge-success'; // Default Hijau (Selesai)
            if (item.status === 'Baru') badgeColor = 'badge-warning'; // Kuning
            if (item.status === 'Sedang Dikerjakan') badgeColor = 'badge-primary'; // Biru
            if (item.status === 'Batal') badgeColor = 'badge-danger'; // Merah

            row.innerHTML = `
                <td>${formatDate(item.tanggal)}</td>
                <td><small>${item.id.substring(0, 8)}...</small></td>
                <td>${escapeHtml(item.customerName)}</td>
                <td>${escapeHtml(item.keluhan)}</td>
                <td>Rp ${formatCurrency(item.totalBayar)}</td>
                <td><span class="badge ${badgeColor}">${item.status || 'Selesai'}</span></td>
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
            // Refresh current table based on endpoint
            if (endpoint.includes('customer')) loadTable('customer');
            if (endpoint.includes('service')) loadTable('service');
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

// Service
function openServiceModal() {
    document.getElementById('service-id').value = '';
    document.getElementById('service-name').value = '';
    document.getElementById('service-price').value = '';
    document.getElementById('modal-service').style.display = 'flex';
}

async function saveService() {
    const service = {
        namaService: document.getElementById('service-name').value,
        harga: parseFloat(document.getElementById('service-price').value)
    };
    await fetch('/api/services', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(service)
    });
    closeModal('modal-service');
    loadTable('service');
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
    loadTable('item');
}

// Transaction (BARU)
async function openTransactionModal(editData = null) {
    // 1. Fetch Data Master
    const [resCust, resServ, resItem] = await Promise.all([
        fetch('/api/customers'),
        fetch('/api/services'),
        fetch('/api/items')
    ]);

    const customers = await resCust.json();
    availableServices = await resServ.json();
    availableItems = await resItem.json();

    // 2. Populate Customer Dropdown
    const select = document.getElementById('transaction-customer');
    select.innerHTML = '<option value="">Pilih Customer</option>';
    customers.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = `${c.nama}`;
        select.appendChild(opt);
    });

    // 3. Populate Service Checkboxes
    const serviceContainer = document.getElementById('service-checkbox-container');
    serviceContainer.innerHTML = '';
    serviceContainer.className = 'service-list'; // Pastikan CSS service-list ada

    availableServices.forEach(s => {
        // Cek apakah service ini dipilih (jika mode edit)
        let isChecked = false;
        if (editData && editData.services) {
            isChecked = editData.services.some(eds => eds.id === s.id); // Cek ID
        }

        const div = document.createElement('div');
        div.className = 'service-item';
        div.innerHTML = `
            <input type="checkbox" id="srv-${s.id}" value="${s.id}" data-price="${s.harga}" onchange="calculateTotal()" ${isChecked ? 'checked' : ''}>
            <label for="srv-${s.id}">
                ${s.namaService} 
                <span class="service-price">(Rp ${formatCurrency(s.harga)})</span>
            </label>
        `;
        serviceContainer.appendChild(div);
    });

    // 4. Reset / Fill Form Data
    const tbody = document.getElementById('transaction-items-body');
    tbody.innerHTML = '';

    if (editData) {
        // --- MODE EDIT ---
        document.getElementById('transaction-id').value = editData.id;
        document.getElementById('transaction-customer').value = editData.customer.id;
        document.getElementById('transaction-date').value = editData.tanggal;
        document.getElementById('transaction-complaint').value = editData.keluhan || '';
        document.getElementById('transaction-status').value = editData.status || 'Baru';

        // Isi barang yang sudah ada
        if (editData.items && editData.items.length > 0) {
            editData.items.forEach(itemData => {
                addTransactionItemRow(itemData.item.id, itemData.quantity);
            });
        }
    } else {
        // --- MODE BARU ---
        document.getElementById('transaction-id').value = '';
        document.getElementById('transaction-date').value = new Date().toISOString().split('T')[0];
        document.getElementById('transaction-complaint').value = '';
        document.getElementById('transaction-status').value = 'Baru';
        document.getElementById('transaction-customer').value = '';
    }

    calculateTotal();
    document.getElementById('modal-transaction').style.display = 'flex';
}

// Menambah baris barang di modal transaksi
function addTransactionItemRow(selectedItemId = null, qtyVal = 1) {
    const tbody = document.getElementById('transaction-items-body');
    const row = document.createElement('tr');

    let options = '<option value="">Pilih Barang</option>';
    availableItems.forEach(i => {
        // Jika mode edit, kita harus hati-hati menampilkan stok.
        // Logikanya: Stok Tampil = Stok Sekarang di Gudang + Quantity yang sedang dipakai di transaksi ini (jika barangnya sama)
        // Tapi untuk simplenya kita tampilkan stok gudang saja.
        const isSelected = selectedItemId && i.id === selectedItemId ? 'selected' : '';
        options += `<option value="${i.id}" data-price="${i.harga}" ${isSelected}>${i.namaBarang} (Stok: ${i.stok})</option>`;
    });

    row.innerHTML = `
        <td style="padding: 5px;">
            <select class="item-select" onchange="calculateTotal()" style="width:100%; padding:5px; background:#0d1117; color:white; border:1px solid #30363d; border-radius:4px;">${options}</select>
        </td>
        <td style="padding: 5px;">
            <input type="number" class="item-qty" value="${qtyVal}" min="1" onchange="calculateTotal()" style="width:100%; padding:5px; background:#0d1117; color:white; border:1px solid #30363d; border-radius:4px;">
        </td>
        <td style="padding: 5px; text-align:center;">
            <button type="button" onclick="this.closest('tr').remove(); calculateTotal()" style="color:#f85149; background:none; border:none; cursor:pointer; font-weight:bold;">✕</button>
        </td>
    `;
    tbody.appendChild(row);
}

// Hitung total estimasi di Frontend
function calculateTotal() {
    let total = 0;

    // Service
    const checkboxes = document.querySelectorAll('#service-checkbox-container input[type="checkbox"]:checked');
    checkboxes.forEach(cb => {
        total += parseFloat(cb.getAttribute('data-price') || 0);
    });

    // Barang
    const rows = document.querySelectorAll('#transaction-items-body tr');
    rows.forEach(row => {
        const select = row.querySelector('.item-select');
        const qtyInput = row.querySelector('.item-qty');

        if (select && select.selectedIndex > 0) {
            const price = parseFloat(select.options[select.selectedIndex].getAttribute('data-price') || 0);
            const qty = parseInt(qtyInput.value || 0);
            total += (price * qty);
        }
    });

    document.getElementById('transaction-display-total').textContent = 'Rp ' + formatCurrency(total);
}

async function editTransaction(id) {
    try {
        const res = await fetch(`/api/transactions/${id}`);
        if (res.ok) {
            const data = await res.json();
            openTransactionModal(data); // Buka modal dengan data
        } else {
            alert("Data transaksi tidak ditemukan");
        }
    } catch (e) {
        console.error(e);
    }
}

async function saveTransaction() {
    // 1. Kumpulkan Service yang dipilih
    const selectedServices = [];
    document.querySelectorAll('#service-checkbox-container input[type="checkbox"]:checked').forEach(cb => {
        selectedServices.push({ id: cb.value });
    });

    // 2. Kumpulkan Barang yang dipilih
    const selectedItems = [];
    document.querySelectorAll('#transaction-items-body tr').forEach(row => {
        const select = row.querySelector('.item-select');
        const qty = row.querySelector('.item-qty').value;
        if (select.value) {
            selectedItems.push({
                item: { id: select.value },
                quantity: parseInt(qty)
            });
        }
    });

    const trans = {
        id: document.getElementById('transaction-id').value || null, // Kirim ID jika edit
        customer: { id: document.getElementById('transaction-customer').value },
        tanggal: document.getElementById('transaction-date').value,
        keluhan: document.getElementById('transaction-complaint').value,
        status: document.getElementById('transaction-status').value, // TAMBAHAN STATUS
        services: selectedServices,
        items: selectedItems
    };

    if (!trans.customer.id) { alert('Pilih customer!'); return; }

    const response = await fetch('/api/transactions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(trans)
    });

    if (response.ok) {
        closeModal('modal-transaction');
        loadTable('transaction');
        loadDashboard();
    } else {
        const msg = await response.text();
        alert('Gagal menyimpan: ' + msg);
    }
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
    const table = document.getElementById('table-report');
    if (table.querySelector('tbody tr').innerText.includes('Tidak ada data')) {
        alert('Tampilkan data laporan terlebih dahulu');
        return;
    }

    const printWindow = window.open('', '_blank', 'width=800,height=600');
    const tableClone = table.cloneNode(true);

    printWindow.document.write(`
        <html><head><title>Cetak Laporan</title>
        <style>table { width: 100%; border-collapse: collapse; font-family: sans-serif; } th, td { border: 1px solid black; padding: 8px; text-align: left; }</style>
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