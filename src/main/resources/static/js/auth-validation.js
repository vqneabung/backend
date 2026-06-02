    /**
 * auth-validation.js — Client-side validation for login & register forms.
 *
 * Uses JustValidate CDN (~10KB, no jQuery dependency):
 *   https://just-validate.dev/
 *
 * Auto-detects which form is on the page by checking for form IDs:
 *   - #loginForm     → applies login validation rules
 *   - #registerForm  → applies register validation rules
 *
 * Validation rules mirror backend validation in RegisterRequest.kt:
 *   - email:    required, valid email format
 *   - password: required, min 6 characters
 *   - confirmPassword (register only): required, must match password
 *
 * NOTE: Backend validation is the source of truth. Client-side validation
 * is a UX enhancement only — always validate on the server.
 */
document.addEventListener('DOMContentLoaded', function () {
    // JustValidate must be loaded via CDN before this script.
    // If the library is not available, skip validation gracefully.
    if (typeof JustValidate === 'undefined') {
        console.warn('JustValidate CDN not loaded — client-side validation skipped');
        return;
    }

    // --- Login form validation ---
    var loginForm = document.getElementById('loginForm');
    if (loginForm) {
        console.log('[auth-validation] Login form found, initializing JustValidate');
        var loginValidator = new JustValidate('#loginForm', {
            errorFieldCssClass: 'just-validate-error-field',
            submitHandler: function () {
                console.log('[auth-validation] Login validation passed, submitting form');
                var form = document.getElementById('loginForm');
                if (form) {
                    form.submit();
                } else {
                    console.error('[auth-validation] loginForm not found in DOM');
                }
            },
            onError: function (errors) {
                console.log('[auth-validation] Validation failed:', errors);
            },
        });

        loginValidator
            .addField('#loginEmail', [
                {
                    rule: 'required',
                    errorMessage: 'Email is required',
                },
                {
                    rule: 'email',
                    errorMessage: 'Please enter a valid email address',
                },
            ])
            .addField('#loginPassword', [
                {
                    rule: 'required',
                    errorMessage: 'Password is required',
                },
            ]);

        // Backup: nếu JustValidate prevent default, submit form trực tiếp
        loginForm.addEventListener('submit', function (e) {
            console.log('[auth-validation] 🔥 Native submit event fired! JustValidate prevented: ' + e.defaultPrevented);
            if (e.defaultPrevented) {
                // JustValidate đã chặn — submit trực tiếp bằng JS
                console.log('[auth-validation] Bypassing JustValidate, submitting form directly');
                this.submit();
            }
        });
    }

    // --- Register form validation ---
    var registerForm = document.getElementById('registerForm');
    if (registerForm) {
        var registerValidator = new JustValidate('#registerForm', {
            errorFieldCssClass: 'just-validate-error-field',
            submitHandler: function () {
                document.getElementById('registerForm').submit();
            },
        });

        registerValidator
            .addField('#registerEmail', [
                {
                    rule: 'required',
                    errorMessage: 'Email is required',
                },
                {
                    rule: 'email',
                    errorMessage: 'Please enter a valid email address',
                },
            ])
            .addField('#registerPassword', [
                {
                    rule: 'required',
                    errorMessage: 'Password is required',
                },
                {
                    rule: 'minLength',
                    value: 6,
                    errorMessage: 'Password must be at least 6 characters',
                },
            ])
            .addField('#registerConfirmPassword', [
                {
                    validator: function (value) {
                        var password = document.getElementById('registerPassword').value;
                        return value === password;
                    },
                    errorMessage: 'Passwords do not match',
                },
            ]);
    }
});
