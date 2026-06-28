(function () {
    function showToast(message) {
        var toast = document.getElementById('toast');
        if (!toast || !message) {
            return;
        }
        toast.textContent = message;
        toast.classList.add('show');
        window.setTimeout(function () {
            toast.classList.remove('show');
        }, 2600);
    }

    function updateFieldState(field) {
        if (!field.required && !field.value) {
            field.classList.remove('is-valid', 'is-invalid');
            return;
        }
        var ok = field.checkValidity();
        field.classList.toggle('is-valid', ok);
        field.classList.toggle('is-invalid', !ok);
        var feedback = field.parentElement.querySelector('.live-feedback');
        if (feedback) {
            feedback.classList.toggle('valid', ok);
            feedback.classList.toggle('invalid', !ok);
        }
    }

    document.querySelectorAll('.needs-live-validation input, .needs-live-validation select, .needs-live-validation textarea').forEach(function (field) {
        field.addEventListener('input', function () {
            updateFieldState(field);
        });
        field.addEventListener('blur', function () {
            updateFieldState(field);
        });
    });

    document.querySelectorAll('.needs-live-validation').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            form.querySelectorAll('input, select, textarea').forEach(updateFieldState);
            if (!form.checkValidity()) {
                event.preventDefault();
                showToast('请先修正表单中的提示项');
            }
        });
    });

    document.querySelectorAll('.confirm-form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            var message = form.getAttribute('data-confirm') || '确定继续？';
            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    });

    var usernameField = document.querySelector('.ajax-username');
    if (usernameField) {
        var timer = null;
        usernameField.addEventListener('input', function () {
            window.clearTimeout(timer);
            timer = window.setTimeout(function () {
                var feedback = usernameField.parentElement.querySelector('.live-feedback');
                if (!usernameField.value || !usernameField.checkValidity()) {
                    updateFieldState(usernameField);
                    return;
                }
                feedback.textContent = '正在检查用户名...';
                fetch(usernameField.dataset.checkUrl + '&username=' + encodeURIComponent(usernameField.value), {
                    headers: {'Accept': 'application/json'}
                }).then(function (response) {
                    if (!response.ok) {
                        throw new Error('网络响应异常');
                    }
                    return response.json();
                }).then(function (data) {
                    if (data.exists) {
                        usernameField.setCustomValidity('用户名已存在');
                        feedback.textContent = '用户名已存在';
                    } else if (!data.valid) {
                        usernameField.setCustomValidity('用户名格式不正确');
                        feedback.textContent = '用户名格式不正确';
                    } else {
                        usernameField.setCustomValidity('');
                        feedback.textContent = '用户名可用';
                    }
                    updateFieldState(usernameField);
                }).catch(function () {
                    feedback.textContent = '检查失败，请稍后重试';
                    feedback.classList.add('invalid');
                });
            }, 350);
        });
    }

    document.querySelectorAll('input[type="file"]').forEach(function (field) {
        field.addEventListener('change', function () {
            var names = Array.prototype.map.call(field.files, function (file) {
                return file.name;
            });
            if (names.length) {
                showToast('已选择：' + names.join('、'));
            }
        });
    });
})();
