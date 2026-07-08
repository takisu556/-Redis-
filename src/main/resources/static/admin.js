const API_BASE = 'http://localhost:8080/api';
let allProducts = [];
let editingProductId = null;


const Images = [
    "./img/1.png",
    "./img/2.png",
    "./img/3.png",
    "./img/4.png",
    "./img/5.png",
    "./img/6.png",
    "./img/7.png",
    "./img/8.png",
    "./img/9.png",
    "./img/10.png",
    "./img/11.png",
];
// 页面加载时获取数据
document.addEventListener('DOMContentLoaded', function() {
//新规则，防止图片未完全加载网页已经显示

    // 设置默认背景色
    document.body.style.backgroundColor = '#f5f5f5';

    // 随机选择图片
    const randomImage = Images[Math.floor(Math.random() * Images.length)];
    
    // 创建图片对象来检测加载状态
    const img = new Image();
    img.onload = function() {
        // 图片加载成功后设置背景
        document.body.style.backgroundImage = `url('${randomImage}')`;
        document.body.style.backgroundSize = 'cover';
        document.body.style.backgroundPosition = 'center';   
        document.body.style.backgroundRepeat = 'no-repeat';
    };
    
    img.onerror = function() {
        // 图片加载失败时，保持默认背景色
        console.warn(`图片加载失败: ${randomImage}`);
        // 可以选择加载另一张图片或保持默认背景
    };
    
    img.src = randomImage;

    refreshProducts();
    // 绑定取消编辑按钮事件
    document.getElementById('cancel-edit').onclick = resetForm;
    // 绑定表单提交事件
    document.getElementById('product-form').onsubmit = handleFormSubmit;
    // 绑定图片上传按钮
    const uploadBtn = document.getElementById('upload-image-btn');
    if (uploadBtn) uploadBtn.onclick = handleUploadImage;
    // 绑定搜索输入事件
    document.getElementById('search-input').oninput = handleSearch;
});

// 刷新商品列表
async function refreshProducts() {
    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/products`);
        if (!response.ok) throw new Error('获取商品失败');
        
        allProducts = await response.json();
        renderProducts(allProducts);
        updateStats(allProducts);
        showToast('数据已刷新', 'success');
    } catch (error) {
        console.error('获取商品失败:', error);
        showToast('获取商品失败: ' + error.message, 'error');
        renderEmpty();
    } finally {
        showLoading(false);
    }
}

// 渲染商品列表
function renderProducts(products) {
    const tbody = document.getElementById('product-tbody');
    
    if (products.length === 0) {
        renderEmpty();
        return;
    }
// 生成商品行，加入空值保护，避免null时调用toLowerCase()或toFixed()报错
    tbody.innerHTML = products.map(p => {
        const category = p.category || '未分类';
        const imgSrc = p.img || 'https://via.placeholder.com/50';
        const nameText = p.name || '';
        const priceText = (typeof p.price === 'number') ? p.price.toFixed(2) : (p.price ? Number(p.price).toFixed(2) : '0.00');
        const quantityText = p.quantity || 0;

        return `
        <tr>
            <td><img src="${imgSrc}" class="product-img" alt="${nameText}"></td>
            <td><strong>${p.id}</strong></td>
            <td>${nameText}</td>
            <td><span class="category-badge category-${category.toLowerCase()}">${category}</span></td>
            <td><strong>¥${priceText}</strong></td>
            <td>${quantityText}</td>
            <td>
                <button class="btn btn-warning btn-small" onclick="editProduct('${p.id}')">编辑</button>
                <button class="btn btn-danger btn-small" onclick="deleteProduct('${p.id}')">删除</button>
            </td>
        </tr>
    `;
    }).join('');
}

// 渲染空状态
function renderEmpty() {
    const tbody = document.getElementById('product-tbody');
    tbody.innerHTML = `
        <tr>
            <td colspan="7">
                <div class="empty-state">
                    <div class="empty-state-icon">📦</div>
                    <h3>暂无商品数据</h3>
                    <p>点击左侧表单添加商品，或点击"初始化数据"加载示例数据</p>
                </div>
            </td>
        </tr>
    `;
}

// 更新统计数据，加入空值保护
function updateStats(products) {
    const totalProducts = products.length;
    const totalStock = products.reduce((sum, p) => sum + (p.quantity || 0), 0);
    const totalValue = products.reduce((sum, p) => sum + ((p.price || 0) * (p.quantity || 0)), 0);
    const categories = new Set(products.map(p => p.category || '').filter(c => c));

    document.getElementById('total-products').textContent = totalProducts;
    document.getElementById('total-stock').textContent = totalStock;
    document.getElementById('total-value').textContent = '¥' + totalValue.toFixed(2);
    document.getElementById('total-categories').textContent = categories.size;
}

// 处理表单提交
async function handleFormSubmit(e) {
    e.preventDefault();

    const product = {
        id: document.getElementById('product-id').value,
        name: document.getElementById('product-name').value,
        price: parseFloat(document.getElementById('product-price').value),
        quantity: parseInt(document.getElementById('product-quantity').value),
        category: document.getElementById('product-category').value,
        img: document.getElementById('product-img').value,
        link: document.getElementById('product-link').value,
        slogan: document.getElementById('product-slogan').value
    };

    try {
        const response = await fetch(`${API_BASE}/products`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(product)
        });

        if (!response.ok) throw new Error('添加/更新失败');

        showToast(editingProductId ? '商品更新成功！' : '商品添加成功！', 'success');
        resetForm();
        refreshProducts();
    } catch (error) {
        console.error('操作失败:', error);
        showToast('操作失败: ' + error.message, 'error');
    }
}

// 编辑商品
async function editProduct(id) {
    const product = allProducts.find(p => p.id === id);
    if (!product) return;

    editingProductId = id;
    document.getElementById('product-id').value = product.id;
    document.getElementById('product-name').value = product.name;
    document.getElementById('product-price').value = product.price;
    document.getElementById('product-quantity').value = product.quantity;
    document.getElementById('product-category').value = product.category;
    document.getElementById('product-img').value = product.img || '';
    document.getElementById('product-link').value = product.link || '';
    document.getElementById('product-slogan').value = product.slogan || '';

    document.getElementById('submit-text').textContent = '💾 更新商品';
    document.getElementById('cancel-edit').style.display = 'block';
    document.getElementById('product-id').readOnly = true;

    // 滚动到表单
    document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });
}

// 取消编辑/重置表单
function resetForm() {
    editingProductId = null;
    document.getElementById('product-form').reset();
    document.getElementById('submit-text').textContent = '✨ 添加商品';
    document.getElementById('cancel-edit').style.display = 'none';
    document.getElementById('product-id').readOnly = false;
}

// 上传图片到后端（multipart/form-data）
async function handleUploadImage() {
    const id = document.getElementById('product-id').value;
    if (!id) {
        alert('请先输入商品 ID 后再上传图片');
        return;
    }
    const fileInput = document.getElementById('product-file');
    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        alert('请选择要上传的图片文件');
        return;
    }
    const file = fileInput.files[0];
    const form = new FormData();
    form.append('file', file);

    try {
        const resp = await fetch(`${API_BASE}/admin/products/${encodeURIComponent(id)}/image`, {
            method: 'POST',
            body: form
        });
        if (!resp.ok) throw new Error('上传失败');
        const data = await resp.json();
        if (data.success) {
            showToast('图片上传成功', 'success');
            // 如果后端返回 dataUri，则填充 img 文本框并显示预览
            if (data.dataUri) {
                const imgInput = document.getElementById('product-img');
                if (imgInput) imgInput.value = data.dataUri;
                const preview = document.getElementById('upload-preview');
                if (preview) { preview.src = data.dataUri; preview.style.display = 'inline-block'; }
            }
            // 刷新列表以显示最新图片
            refreshProducts();
            fileInput.value = '';
        } else {
            showToast('图片上传失败: ' + (data.message || ''), 'error');
        }
    } catch (err) {
        console.error('上传失败', err);
        showToast('上传失败: ' + err.message, 'error');
    }
}

// 删除商品
async function deleteProduct(id) {
    if (!confirm('确定要删除这个商品吗？')) return;

    try {
        const response = await fetch(`${API_BASE}/products/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('删除失败');

        showToast('商品删除成功！', 'success');
        refreshProducts();
    } catch (error) {
        console.error('删除失败:', error);
        showToast('删除失败: ' + error.message, 'error');
    }
}

// 初始化数据
async function initializeData() {
    if (!confirm('确定要初始化示例数据吗？这将添加16个示例商品。')) return;

    try {
        const response = await fetch(`${API_BASE}/admin/products/init`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('初始化失败');

        const result = await response.json();
        showToast(`初始化成功！已添加 ${result.total} 个商品`, 'success');
        refreshProducts();
    } catch (error) {
        console.error('初始化失败:', error);
        showToast('初始化失败: ' + error.message, 'error');
    }
}

// 清空所有商品
async function clearAllProducts() {
    if (!confirm('⚠️ 警告：确定要删除所有商品吗？此操作不可恢复！')) return;

    try {
        const response = await fetch(`${API_BASE}/admin/products/all`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('清空失败');

        showToast('所有商品已清空！', 'success');
        refreshProducts();
    } catch (error) {
        console.error('清空失败:', error);
        showToast('清空失败: ' + error.message, 'error');
    }
}

// 处理搜索
function handleSearch(e) {
    const value = (e && e.target && e.target.value) ? e.target.value : '';
    const keyword = value.toLowerCase();
    const filtered = allProducts.filter(p =>
        (p.name || '').toLowerCase().includes(keyword) ||
        (p.category || '').toLowerCase().includes(keyword) ||
        (p.id || '').includes(keyword)
    );
    renderProducts(filtered);
}

// 显示提示
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.className = `toast ${type} show`;
    toast.querySelector('.toast-icon').textContent = type === 'success' ? '✅' : '❌';
    toast.querySelector('.toast-message').textContent = message;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// 显示/隐藏加载状态
function showLoading(show) {
    document.getElementById('loading').style.display = show ? 'block' : 'none';
    document.getElementById('products-container').style.display = show ? 'none' : 'block';
}