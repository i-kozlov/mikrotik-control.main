// ===== ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ =====
let toastElement, toastMessage, toast;

// ===== ИНИЦИАЛИЗАЦИЯ =====
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Подготовка уведомлений
    toastElement = document.getElementById('notification');
    toastMessage = document.getElementById('notification-message');
    toast = new bootstrap.Toast(toastElement, { delay: 3000 });
    
    // Инициализация обработчиков групп
    initializeGroupHandlers();
    
    // Инициализация обработчиков правил
    initializeRuleHandlers();
    
    // Инициализация фильтров
    initializeFilters();
    
    // Инициализация мобильных фильтров
    initializeMobileFilters();
}

// ===== ОБРАБОТЧИКИ ГРУПП =====
function initializeGroupHandlers() {
    const expandAllBtn = document.getElementById('expand-all-groups');
    const collapseAllBtn = document.getElementById('collapse-all-groups');
    
    if (expandAllBtn) {
        expandAllBtn.addEventListener('click', function() {
            document.querySelectorAll('.group-content.collapse').forEach(group => {
                if (!group.classList.contains('show')) {
                    const collapse = new bootstrap.Collapse(group, { show: true });
                }
            });
            updateAllGroupIcons();
        });
    }
    
    if (collapseAllBtn) {
        collapseAllBtn.addEventListener('click', function() {
            document.querySelectorAll('.group-content.collapse.show').forEach(group => {
                const collapse = new bootstrap.Collapse(group, { hide: true });
            });
            updateAllGroupIcons();
        });
    }
    
    // Обработчик для изменения иконок при сворачивании/разворачивании групп
    document.querySelectorAll('.group-content').forEach(groupContent => {
        groupContent.addEventListener('shown.bs.collapse', function() {
            updateGroupIcon(this.id, false);
        });
        
        groupContent.addEventListener('hidden.bs.collapse', function() {
            updateGroupIcon(this.id, true);
        });
    });
}

function updateGroupIcon(groupId, collapsed) {
    const groupName = groupId.replace('group-', '');
    const icon = document.querySelector('#group-header-' + groupName + ' .group-toggle-icon');
    if (icon) {
        if (collapsed) {
            icon.classList.add('collapsed');
        } else {
            icon.classList.remove('collapsed');
        }
    }
}

function updateAllGroupIcons() {
    document.querySelectorAll('.group-content').forEach(groupContent => {
        const isCollapsed = !groupContent.classList.contains('show');
        updateGroupIcon(groupContent.id, isCollapsed);
    });
}

// ===== ОБРАБОТЧИКИ ПРАВИЛ =====
function initializeRuleHandlers() {
    // Переключение правила (AJAX)
    document.querySelectorAll('.toggle-rule').forEach(button => {
        button.addEventListener('click', function() {
            const uid = this.dataset.uid;
            const enable = this.dataset.enable === 'true';
            
            // Отключаем кнопку на время запроса
            this.disabled = true;
            
            fetch(`/api/rules/${uid}/toggle?enable=${enable}`, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                // Получаем актуальный статус правила после изменения
                return refreshRuleStatus(uid);
            })
            .then(actualData => {
                // Включаем кнопку обратно
                this.disabled = false;
                showNotification(`Правило успешно ${enable ? 'включено' : 'отключено'}`, true);
            })
            .catch(error => {
                // Включаем кнопку обратно в случае ошибки
                this.disabled = false;
                showNotification('Ошибка при выполнении запроса: ' + error, false);
                console.error('Ошибка:', error);
            });
        });
    });
    
    // Временное изменение состояния правила
    document.querySelectorAll('.enable-temporary').forEach(button => {
        button.addEventListener('click', function() {
            const uid = this.dataset.uid;
            const minutes = this.dataset.minutes;
            
            // Найдем карточку правила
            const ruleCard = document.getElementById('rule-' + uid);
            
            // Определяем текущее состояние правила и целевое состояние для планировщика
            const isCurrentlyEnabled = ruleCard.classList.contains('rule-enabled');
            
            // Определяем целевое состояние (противоположное текущему)
            const targetState = !isCurrentlyEnabled;
            
            // Сначала изменяем состояние правила
            fetch(`/api/rules/${uid}/toggle?enable=${targetState}`, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                // Планируем возврат в исходное состояние
                return fetch(`/api/rules/${uid}/schedule?targetState=${isCurrentlyEnabled}&minutes=${minutes}`, {
                    method: 'POST'
                });
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Получаем актуальный статус правила после всех изменений
                    return refreshRuleStatus(uid);
                } else {
                    throw new Error(data.error || 'Ошибка при планировании');
                }
            })
            .then(actualData => {
                const action = targetState ? 'включено' : 'отключено';
                showNotification(`Правило временно ${action} на ${minutes} минут`, true);
            })
            .catch(error => {
                showNotification('Ошибка при выполнении запроса: ' + error.message, false);
                console.error('Ошибка:', error);
            });
        });
    });
    
    // Обновление статуса правила
    document.querySelectorAll('.refresh-rule').forEach(button => {
        button.addEventListener('click', function() {
            const uid = this.dataset.uid;
            
            // Отключаем кнопку на время запроса
            this.disabled = true;
            
            fetch(`/api/rules/${uid}`)
                .then(response => response.json())
                .then(data => {
                    // Включаем кнопку обратно
                    this.disabled = false;
                    updateRuleCard(data);
                    updateGroupStats();
                    showNotification('Статус правила обновлен', true);
                })
                .catch(error => {
                    // Включаем кнопку обратно в случае ошибки
                    this.disabled = false;
                    showNotification('Ошибка при обновлении статуса: ' + error, false);
                    console.error('Ошибка:', error);
                });
        });
    });
}

// ===== ПОЛУЧЕНИЕ АКТУАЛЬНОГО СТАТУСА ПРАВИЛА =====
function refreshRuleStatus(uid) {
    return fetch(`/api/rules/${uid}`)
        .then(response => response.json())
        .then(data => {
            updateRuleCard(data);
            updateGroupStats();
            return data;
        })
        .catch(error => {
            console.error('Ошибка при получении актуального статуса:', error);
            throw error;
        });
}

// ===== ОБНОВЛЕНИЕ КАРТОЧКИ ПРАВИЛА =====
function updateRuleCard(rule) {
    const ruleCard = document.getElementById('rule-' + rule.uid);
    if (!ruleCard) return;
    
    // Обновляем классы для стилизации
    if (rule.enabled) {
        ruleCard.classList.add('rule-enabled');
        ruleCard.classList.remove('rule-disabled');
    } else {
        ruleCard.classList.add('rule-disabled');
        ruleCard.classList.remove('rule-enabled');
    }
    
    // Обновляем стиль карточки
    const card = ruleCard.querySelector('.card');
    if (rule.enabled) {
        card.classList.add('enabled', 'enabled-bg');
        card.classList.remove('disabled', 'disabled-bg');
    } else {
        card.classList.add('disabled', 'disabled-bg');
        card.classList.remove('enabled', 'enabled-bg');
    }
    
    // Обновляем видимость бейджей
    const scheduledBadge = ruleCard.querySelector('.scheduled-badge');
    if (scheduledBadge) {
        scheduledBadge.style.display = rule.scheduled ? '' : 'none';
    }
    
    const inactiveTimeBadge = ruleCard.querySelector('.inactive-time-badge');
    if (inactiveTimeBadge) {
        inactiveTimeBadge.style.display = rule.inactiveTime ? '' : 'none';
    }
    
    const autoOnBadge = ruleCard.querySelector('.auto-on-badge');
    if (autoOnBadge) {
        autoOnBadge.style.display = rule.autoOn ? '' : 'none';
    }
    
    const autoOffBadge = ruleCard.querySelector('.auto-off-badge');
    if (autoOffBadge) {
        autoOffBadge.style.display = rule.autoOff ? '' : 'none';
    }
    
    // Обновляем только бейдж статуса
    const statusBadge = ruleCard.querySelector('.status-badge');
    if (statusBadge) {
        statusBadge.className = 'badge status-badge ' + (rule.enabled ? 'bg-success' : 'bg-secondary');
        statusBadge.textContent = rule.enabled ? 'Активно' : 'Неактивно';
    }
    
    // Обновляем ВСЕ кнопки переключения (мобильная и десктопная)
    const toggleButtons = ruleCard.querySelectorAll('.toggle-rule');
    toggleButtons.forEach(toggleButton => {
        // Определяем тип кнопки по классам
        const isMobile = toggleButton.closest('.d-block.d-md-none') !== null;
        const isDesktop = toggleButton.closest('.d-none.d-md-flex') !== null;
        
        // Скрываем или показываем кнопку в зависимости от hideToggle
        if (rule.hideToggle) {
            toggleButton.style.display = 'none';
        } else {
            toggleButton.style.display = '';
            
            if (isMobile) {
                // Мобильная версия - обычные цвета
                toggleButton.className = 'btn toggle-rule ' + 
                                      (rule.enabled ? 'btn-danger' : 'btn-success');
            } else if (isDesktop) {
                // Десктопная версия - outline цвета + btn-sm
                toggleButton.className = 'btn btn-sm toggle-rule ' + 
                                      (rule.enabled ? 'btn-outline-danger' : 'btn-outline-success');
            }
            
            toggleButton.textContent = rule.enabled ? 'Отключить' : 'Включить';
            toggleButton.dataset.enable = (!rule.enabled).toString();
            
            // Сбрасываем оригинальные данные для корректной работы блокировки
            toggleButton.dataset.originalText = toggleButton.textContent;
            toggleButton.dataset.originalClass = toggleButton.className;
        }
    });
    
    // Обновляем контейнеры кнопок для правильного отображения (мобильная версия)
    const mobileActionsContainer = ruleCard.querySelector('.primary-actions');
    if (mobileActionsContainer) {
        mobileActionsContainer.style.display = rule.hideToggle ? 'none' : '';
    }
}

// ===== ОБНОВЛЕНИЕ СТАТИСТИКИ ГРУППЫ =====
function updateGroupStats() {
    document.querySelectorAll('.group-section').forEach(groupSection => {
        const groupContent = groupSection.querySelector('.group-content');
        const groupStats = groupSection.querySelector('.group-stats');
        
        if (groupContent && groupStats) {
            const allRules = groupContent.querySelectorAll('.rule-item');
            const enabledRules = groupContent.querySelectorAll('.rule-item.rule-enabled');
            
            groupStats.innerHTML = `<span>${allRules.length}</span> правил (<span>${enabledRules.length}</span> активных)`;
        }
    });
}

// ===== ФИЛЬТРЫ =====
function initializeFilters() {
    // Фильтрация по типу
    document.querySelectorAll('.filter-btn').forEach(button => {
        button.addEventListener('click', function() {
            const filter = this.dataset.filter;
            document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            filterRules();
        });
    });
    
    // Фильтрация по состоянию
    document.querySelectorAll('.filter-state-btn').forEach(button => {
        button.addEventListener('click', function() {
            const stateFilter = this.dataset.state;
            document.querySelectorAll('.filter-state-btn').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            filterRules();
        });
    });
    
    // Поиск по описанию
    document.getElementById('rule-search').addEventListener('input', function() {
        filterRules();
    });
}

function filterRules() {
    const typeFilter = document.querySelector('.filter-btn.active').dataset.filter;
    const stateFilter = document.querySelector('.filter-state-btn.active').dataset.state;
    const searchText = document.getElementById('rule-search').value.toLowerCase();
    
    // Сначала обрабатываем все правила
    let hasVisibleRules = false;
    
    document.querySelectorAll('.rule-item').forEach(item => {
        const ruleType = item.dataset.type;
        const isEnabled = item.classList.contains('rule-enabled');
        const description = item.querySelector('.rule-title').textContent.toLowerCase();
        
        const matchesType = typeFilter === 'all' || ruleType === typeFilter;
        const matchesState = stateFilter === 'all' || 
                            (stateFilter === 'enabled' && isEnabled) || 
                            (stateFilter === 'disabled' && !isEnabled);
        const matchesSearch = description.includes(searchText);
        
        if (matchesType && matchesState && matchesSearch) {
            item.style.display = '';
            hasVisibleRules = true;
        } else {
            item.style.display = 'none';
        }
    });
    
    // Затем скрываем/показываем группы в зависимости от наличия видимых правил
    document.querySelectorAll('.group-section').forEach(groupSection => {
        const groupContent = groupSection.querySelector('.group-content');
        const visibleRulesInGroup = groupContent.querySelectorAll('.rule-item[style=""], .rule-item:not([style])');
        
        if (visibleRulesInGroup.length > 0) {
            groupSection.style.display = '';
        } else {
            groupSection.style.display = 'none';
        }
    });
}

// ===== УВЕДОМЛЕНИЯ =====
function showNotification(message, isSuccess) {
    toastElement.classList.remove('text-bg-success', 'text-bg-danger');
    toastElement.classList.add(isSuccess ? 'text-bg-success' : 'text-bg-danger');
    toastMessage.textContent = message;
    toast.show();
}

// ===== МОБИЛЬНЫЕ ФИЛЬТРЫ =====
function initializeMobileFilters() {
    const mobileFiltersToggle = document.querySelector('[data-bs-target="#mobile-filters"]');
    const mobileFilters = document.getElementById('mobile-filters');
    
    if (mobileFiltersToggle && mobileFilters) {
        // Обработчик для поворота иконки
        mobileFilters.addEventListener('shown.bs.collapse', function() {
            const icon = mobileFiltersToggle.querySelector('.bi-chevron-down');
            if (icon) {
                icon.style.transform = 'rotate(180deg)';
            }
        });
        
        mobileFilters.addEventListener('hidden.bs.collapse', function() {
            const icon = mobileFiltersToggle.querySelector('.bi-chevron-down');
            if (icon) {
                icon.style.transform = 'rotate(0deg)';
            }
        });
    }
}