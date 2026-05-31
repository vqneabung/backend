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
        var loginValidator = new JustValidate('#loginForm', {
            errorFieldCssClass: 'just-validate-error-field',
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
    }

    // --- Register form validation ---
    var registerForm = document.getElementById('registerForm');
    if (registerForm) {
        var registerValidator = new JustValidate('#registerForm', {
            errorFieldCssClass: 'just-validate-error-field',
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
