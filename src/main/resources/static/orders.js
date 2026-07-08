const API_BASE = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', function() {
    fetchOrders();
});

async function fetchOrders() {
    try {
        const resp = await fetch(`${API_BASE}/admin/orders`);
        if (!resp.ok) throw new Error('获取订单失败');
        const orders = await resp.json();
        renderOrders(orders);
    } catch (err) {
        console.error(err);
        showToast('获取订单失败: ' + err.message, 'error');
    }
}

function renderOrders(orders) {
    const tbody = document.getElementById('orders-tbody');
    if (!orders || orders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5">暂无订单记录</td></tr>`;
        return;
    }
    tbody.innerHTML = orders.map(o => {
        const created = o.createdAt ? new Date(o.createdAt).toLocaleString() : '';
        const itemsHtml = (o.items || []).map(it => `${it.name} x${it.quantity}`).join('<br/>');
        return `
        <tr>
            <td>${o.id || ''}</td>
            <td>${o.userId || ''}</td>
            <td>${created}</td>
            <td>¥${(o.total||0).toFixed(2)}</td>
            <td>${itemsHtml}</td>
        </tr>
        `;
    }).join('');
}

function showToast(message, type='success') {
    const toast = document.getElementById('toast');
    toast.className = `toast ${type} show`;
    toast.querySelector('.toast-icon').textContent = type === 'success' ? '✅' : '❌';
    toast.querySelector('.toast-message').textContent = message;
    setTimeout(() => toast.classList.remove('show'), 3000);
}
