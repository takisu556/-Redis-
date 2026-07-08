async function fetchUsers() {
    try {
        const res = await fetch('/api/admin/users');
        if (!res.ok) throw new Error('获取用户失败');
        const users = await res.json();
        renderUsers(users);
    } catch (e) {
        showToast('获取用户失败', 'error');
        console.error(e);
    }
}

function renderUsers(users) {
    const tbody = document.getElementById('users-tbody');
    tbody.innerHTML = '';
    users.forEach(u => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${escapeHtml(u.username || '')}</td>
            <td>${escapeHtml(u.displayName || '')}</td>
            <td>${escapeHtml(u.id || '')}</td>
            <td class="users-actions">
                <button class="btn btn-danger btn-small" data-id="${u.id}">删除</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    tbody.querySelectorAll('button[data-id]').forEach(b => {
        b.onclick = async () => {
            const id = b.getAttribute('data-id');
            if (!confirm('确认删除该用户？')) return;
            try {
                const res = await fetch('/api/admin/users/' + encodeURIComponent(id), { method: 'DELETE' });
                if (!res.ok) throw new Error('删除失败');
                showToast('删除成功');
                fetchUsers();
            } catch (e) {
                showToast('删除失败', 'error');
                console.error(e);
            }
        };
    });
}

function escapeHtml(s){ return (s+'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }

function showToast(message, type='success'){
    const root = document.getElementById('toast-root');
    const div = document.createElement('div');
    div.className = 'toast' + (type==='success' ? '' : ' error');
    div.innerHTML = `<span class="toast-icon">${type==='success'?'✅':'❌'}</span><span class="toast-message">${message}</span>`;
    root.appendChild(div);
    setTimeout(()=>div.classList.add('show'),20);
    setTimeout(()=>{ div.classList.remove('show'); setTimeout(()=>div.remove(),300); },3000);
}

document.getElementById('refresh-users').addEventListener('click', fetchUsers);
document.addEventListener('DOMContentLoaded', fetchUsers);
