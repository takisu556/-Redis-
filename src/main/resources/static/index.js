// API 基础地址
const API_BASE = 'http://localhost:8080/api';
const userId = 'user1'; // 实际项目中应该从登录状态获取

// 商品数据（从后端加载）
let products = [];
let currentCategory = "all";
let currentKeyword = "";
let cart = {};
let sidebarCollapsed = false;
let currentPage = 1;
const itemsPerPage = 8;

// 轮播数据
const banners = [
    {
        img: "https://cdn.cnbj1.fds.api.mi-img.com/mi-mall/fbf328138967e62f0896d8af3c00771f.jpg?w=2452&h=920",
        link: "https://www.mi.com/shop/buy/detail?product_id=21305"
    },
    {
        img: "https://cdn.cnbj1.fds.api.mi-img.com/mi-mall/144cc7e6d9ae79310d1cd8b1a2bf2db7.jpg?thumb=1&w=1839&h=690&f=webp&q=90",
        link: "https://www.mi.com/shop/buy/detail?product_id=21130"
    }
];

// ========== API 调用函数 ==========

// 获取所有商品
async function fetchProducts() {
    try {
        const response = await fetch(`${API_BASE}/products`);
        if (!response.ok) throw new Error('获取商品失败');
        products = await response.json();
        renderProducts(false);
    } catch (error) {
        console.error('获取商品失败:', error);
        showToast('加载商品失败，请稍后重试', 'error');
    }
}

// 获取购物车
async function fetchCart() {
    try {
        const response = await fetch(`${API_BASE}/cart/${userId}`);
        if (!response.ok) throw new Error('获取购物车失败');
        const cartData = await response.json();
        cart = cartData;
        renderCart();
    } catch (error) {
        console.error('获取购物车失败:', error);
    }
}

// 添加到购物车
async function addToCartAPI(productId, quantity = 1) {
    try {
        const response = await fetch(`${API_BASE}/cart/${userId}/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productId: productId.toString(),
                quantity: quantity
            })
        });
        
        if (!response.ok) throw new Error('添加失败');
        
        const result = await response.json();
        if (result.success) {
            cart = result.cart;
            renderCart();
            showToast('已添加到购物车');
        }
    } catch (error) {
        console.error('添加到购物车失败:', error);
        showToast('添加失败，请重试', 'error');
    }
}

// 从购物车移除
async function removeFromCartAPI(productId, quantity) {
    try {
        const response = await fetch(`${API_BASE}/cart/${userId}/remove`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productId: productId.toString(),
                quantity: quantity || cart[productId] // 如果没指定数量，则移除全部
            })
        });
        
        if (!response.ok) throw new Error('移除失败');
        
        const result = await response.json();
        cart = result.cart;
        renderCart();
        showToast('已从购物车移除');
    } catch (error) {
        console.error('移除失败:', error);
        showToast('移除失败，请重试', 'error');
    }
}

// 结算购物车
async function checkoutAPI() {
    try {
        const response = await fetch(`${API_BASE}/cart/${userId}/checkout`, {
            method: 'POST'
        });
        
        if (!response.ok) throw new Error('结算失败');
        
        const result = await response.json();
        if (result.success) {
            cart = {};
            renderCart();
            showToast('结算成功！感谢您的购买');
            // 刷新商品列表（更新库存）
            fetchProducts();
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        console.error('结算失败:', error);
        showToast('结算失败，请重试', 'error');
    }
}

// ========== 渲染函数 ==========

function renderProducts(animate = true, append = false) {
    const list = document.getElementById('product-list');
    let filtered = products.filter(p =>
        (currentCategory === "all" || p.category === currentCategory) &&
        (currentKeyword.trim() === "" || p.name.includes(currentKeyword.trim()))
    );

    const start = 0;
    const end = currentPage * itemsPerPage;
    const showList = filtered.slice(start, end);

    if (append && showList.length > list.children.length) {
        const startIndex = list.children.length;
        for (let i = startIndex; i < showList.length; i++) {
            const p = showList[i];
            list.appendChild(createProductElement(p, i - startIndex, animate));
        }
    } else {
        list.innerHTML = '';
        showList.forEach((p, i) => {
            list.appendChild(createProductElement(p, i, animate));
        });
    }

    list.classList.remove('four', 'five');
    list.classList.add(sidebarCollapsed ? 'five' : 'four');

    if (filtered.length === 0) {
        list.innerHTML = "<div style='grid-column: 1/-1; text-align:center; padding:40px;'>没有找到相关商品</div>";
    }
}

function createProductElement(p, index, animate) {
    const div = document.createElement('div');
    div.className = 'product hide';
    // 将链接绑定到整个卡片，方便点击任意位置跳转
    div.setAttribute('data-link', p.link || '#');
    const rating = (Math.random() * 2 + 3).toFixed(1);
    const reviews = Math.floor(Math.random() * 1000 + 100);
    
    div.innerHTML = `
        <div class="product-inner">
            <div class="product-front">
                <img src="${p.img || 'placeholder.jpg'}" alt="${p.name}" class="product-img" data-link="${p.link || '#'}" />
                <div class="product-name">${p.name}</div>
                <div class="product-slogan">${p.slogan || ''}</div>
                <div class="product-price">￥${p.price.toFixed(2)}</div>
                <div class="product-stock">库存: ${p.quantity || 0}</div>
                <button class="add-cart-btn" data-id="${p.id}">加入购物车</button>
            </div>
            <div class="product-back">
                <div class="product-rating">
                    ${'★'.repeat(Math.floor(rating))}${'☆'.repeat(5 - Math.ceil(rating))}
                </div>
                <div class="product-rating-number">${rating} 分</div>
                <div class="product-reviews">${reviews} 条评价</div>
                <button class="add-cart-btn" data-id="${p.id}">加入购物车</button>
            </div>
        </div>
    `;
    
    if (animate) {
        setTimeout(() => div.classList.remove('hide'), 50 * index);
    } else {
        div.classList.remove('hide');
    }
    
    return div;
}

function renderCart() {
    const cartList = document.getElementById('cart-list');
    const cartCount = document.getElementById('cart-count');
    const cartTotal = document.getElementById('cart-total');
    
    let total = 0;
    let count = 0;
    cartList.innerHTML = '';
    
    for (const id in cart) {
        const product = products.find(p => p.id == id);
        if (!product) continue;
        
        const quantity = cart[id];
        total += product.price * quantity;
        count += quantity;
        
        const item = document.createElement('div');
        item.className = 'cart-box-item';
        item.innerHTML = `
            <img class="cart-img" src="${product.img}" alt="${product.name}">
            <div class="cart-info">
                <div>${product.name}</div>
                <div style="font-size:13px;color:#ff375f;">￥${product.price} × ${quantity}</div>
            </div>
            <button class="cart-remove" data-id="${id}">移除</button>
        `;
        cartList.appendChild(item);
    }
    
    cartCount.textContent = count;
    cartTotal.innerHTML = `<b>总计：</b>￥${total.toFixed(2)}`;
    
    cartList.querySelectorAll('.cart-remove').forEach(btn => {
        btn.onclick = function() {
            const id = this.getAttribute('data-id');
            removeFromCartAPI(id);
        };
    });
}

// ========== 事件监听 ==========

// 产品列表点击事件
document.getElementById('product-list').addEventListener('click', function(e) {
    if (e.target.classList.contains('product-img')) {
        const link = e.target.getAttribute('data-link');
        if (link && link !== '#') window.open(link, '_blank');
        return;
    }
    
    if (e.target.tagName === 'BUTTON' && e.target.classList.contains('add-cart-btn')) {
        const id = e.target.getAttribute('data-id');
        addToCartAPI(id, 1);
    }
    // 如果点击在商品卡片的其他位置，尝试读取卡片上的 data-link 并跳转
    const productCard = e.target.closest('.product');
    if (productCard) {
        // 如果点击的是按钮则已被上面处理，这里排除按钮
        if (e.target.tagName === 'BUTTON' || e.target.closest('button')) return;
        const link = productCard.getAttribute('data-link');
        if (link && link !== '#') window.open(link, '_blank');
    }
});

// 购物车展开/收起
const cartBox = document.getElementById('cart');
const cartHeader = document.getElementById('cart-header');
cartHeader.onclick = function() {
    cartBox.classList.toggle('open');
    if (cartBox.classList.contains('open')) {
        fetchCart(); // 展开时刷新购物车
    }
};

// 结算按钮
document.getElementById('checkout-btn').onclick = function() {
    if (Object.keys(cart).length === 0) {
        showToast('购物车为空！', 'error');
        return;
    }
    checkoutAPI();
    cartBox.classList.remove('open');
};

// 分类菜单
document.getElementById('category-menu').addEventListener('click', function(e) {
    if (e.target.tagName === 'BUTTON') {
        currentCategory = e.target.getAttribute('data-category');
        currentKeyword = "";
        document.getElementById('search-input').value = "";
        
        Array.from(this.querySelectorAll('button')).forEach(btn => {
            btn.classList.remove('active');
        });
        e.target.classList.add('active');
        
        currentPage = 1;
        renderProducts(true);
    }
});

// 搜索功能
document.getElementById('search-btn').addEventListener('click', function() {
    currentKeyword = document.getElementById('search-input').value.trim();
    currentPage = 1;
    renderProducts();
});

document.getElementById('search-input').addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        currentKeyword = this.value.trim();
        currentPage = 1;
        renderProducts();
    }
});

// 侧边栏折叠
const sidebar = document.getElementById('sidebar');
const sidebarToggle = document.getElementById('sidebar-toggle');
const categoryList = document.getElementById('category-menu');

sidebarToggle.onclick = function() {
    sidebarCollapsed = !sidebarCollapsed;
    if (sidebarCollapsed) {
        sidebar.classList.add('sidebar-collapsed');
        sidebarToggle.innerHTML = '&#x25B6;';
        categoryList.classList.remove('expanded');
        categoryList.classList.add('collapsed');
    } else {
        sidebar.classList.remove('sidebar-collapsed');
        sidebarToggle.innerHTML = '&#x25C0;';
        categoryList.classList.remove('collapsed');
        categoryList.classList.add('expanded');
    }
    renderProducts();
};

// 滚动加载更多
window.addEventListener('scroll', function() {
    const scrollY = window.scrollY || window.pageYOffset;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;
    
    if (scrollY + windowHeight >= documentHeight - 100) {
        const filtered = products.filter(p =>
            (currentCategory === "all" || p.category === currentCategory) &&
            (currentKeyword.trim() === "" || p.name.includes(currentKeyword.trim()))
        );
        
        if (currentPage * itemsPerPage < filtered.length) {
            currentPage++;
            renderProducts(true, true);
        }
    }
});

// Toast 提示
function showToast(message, type = 'success') {
    const existingToast = document.querySelector('.toast');
    if (existingToast) existingToast.remove();

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <span class="toast-icon">${type === 'success' ? '✅' : '❌'}</span>
        <span class="toast-message">${message}</span>
    `;
    document.body.appendChild(toast);

    setTimeout(() => toast.classList.add('show'), 100);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 轮播初始化
function initBanner() {
    const wrapper = document.querySelector('.banner-wrapper');
    const dots = document.querySelector('.banner-dots');
    let currentIndex = 0;
    
    banners.forEach((banner, index) => {
        const item = document.createElement('div');
        item.className = 'banner-item';
        item.innerHTML = `<a href="${banner.link}" target="_blank"><img src="${banner.img}" alt=""></a>`;
        wrapper.appendChild(item);
        
        const dot = document.createElement('div');
        dot.className = 'banner-dot' + (index === 0 ? ' active' : '');
        dot.onclick = () => goToSlide(index);
        dots.appendChild(dot);
    });
    
    function nextSlide() {
        currentIndex = (currentIndex + 1) % banners.length;
        updateSlider();
    }
    
    let autoplay = setInterval(nextSlide, 5000);
    
    function goToSlide(index) {
        currentIndex = index;
        updateSlider();
        clearInterval(autoplay);
        autoplay = setInterval(nextSlide, 5000);
    }
    
    function updateSlider() {
        wrapper.style.transform = `translateX(-${currentIndex * 100}%)`;
        dots.querySelectorAll('.banner-dot').forEach((dot, index) => {
            dot.classList.toggle('active', index === currentIndex);
        });
    }
    
    document.querySelector('.banner-prev').onclick = () => {
        currentIndex = (currentIndex - 1 + banners.length) % banners.length;
        updateSlider();
        clearInterval(autoplay);
        autoplay = setInterval(nextSlide, 5000);
    };
    
    document.querySelector('.banner-next').onclick = () => {
        currentIndex = (currentIndex + 1) % banners.length;
        updateSlider();
        clearInterval(autoplay);
        autoplay = setInterval(nextSlide, 5000);
    };
}

// ========== 页面初始化 ==========
document.addEventListener('DOMContentLoaded', function() {
    currentCategory = "all";
    currentKeyword = "";
    currentPage = 1;
    document.getElementById('search-input').value = "";
    
    // 从后端加载数据
    fetchProducts();
    fetchCart();
    initBanner();
});