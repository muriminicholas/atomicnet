document.addEventListener('DOMContentLoaded', function () {
    const baseUrl = 'https://atomichotspotapplication.onrender.com/api';

    const showMessage = (text, isError = false, event) => {
        const messageDiv = document.createElement('div');
        messageDiv.textContent = text;
        messageDiv.className = `p-4 rounded-lg shadow-lg ${isError ? 'bg-red-100 text-red-500' : 'bg-green-100 text-green-800'}`;
        messageDiv.style.position = 'absolute';
        messageDiv.style.zIndex = '1000'; // Ensure it appears above other elements

        // Get the button's position and dimensions
        const buttonRect = event.target.getBoundingClientRect();
        const buttonTop = buttonRect.top + window.scrollY; // Account for scroll
        const buttonLeft = buttonRect.left + window.scrollX;
        const buttonWidth = buttonRect.width;

        // Position the message above the button, centered horizontally
        messageDiv.style.top = `${buttonTop - 60}px`; // 60px above the button
        messageDiv.style.left = `${buttonLeft + buttonWidth / 2}px`;
        messageDiv.style.transform = 'translateX(-50%)'; // Center horizontally

        document.body.appendChild(messageDiv);
        setTimeout(() => messageDiv.remove(), 3000);
    };

    const showPaymentPrompt = (packageType, amount, duration, bandwidth) => {
        const modal = document.createElement('div');
        modal.id = 'payment-modal';
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center';
        modal.innerHTML = `
            <div class="bg-white p-6 rounded-lg shadow-lg max-w-sm w-full">
                <h2 class="text-lg font-semibold mb-4 text-pink-600">Payment for ${packageType.replace('_', ' ')}</h2>
                <p class="mb-4">To activate your ${packageType.replace('_', ' ')} package (${duration} hours, ${bandwidth} Mbps), enter your MPESA phone number:</p>
                <input id="mpesa-phone" type="text" placeholder="e.g., +2547XXXXXXXX" class="w-full mb-4 p-2 border rounded focus:ring-2 focus:ring-pink-300" />
                <p class="mb-4">Amount: Ksh.${amount}. You will receive an MPESA STK push to enter your PIN.</p>
                <div class="flex gap-4">
                    <button id="pay-button" class="bg-gradient-to-r from-green-400 to-green-500 text-white p-2 rounded w-full hover:opacity-90">Pay</button>
                    <button id="close-modal" class="bg-gray-200 text-gray-800 p-2 rounded w-full hover:bg-gray-300">Cancel</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        document.getElementById('pay-button').addEventListener('click', async (event) => {
            const phoneNumber = document.getElementById('mpesa-phone').value.trim();
            if (!phoneNumber || !/^\+2547\d{8}$/.test(phoneNumber)) {
                showMessage("Please enter a valid MPESA phone number (e.g., +2547XXXXXXXX)", true, event);
                return;
            }
            try {
                const response = await fetch(`${baseUrl}/initiate_payment`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ phoneNumber, packageType, amount })
                });
                const result = await response.json();
                if (response.ok) {
                    showMessage("A request is sent to your phone. Enter your MPESA PIN to complete payment.", false, event);
                    modal.remove();
                } else {
                    showMessage(result.message, true, event);
                }
            } catch (error) {
                showMessage("Failed to initiate payment: " + error.message, true, event);
            }
        });

        document.getElementById('close-modal').addEventListener('click', () => modal.remove());
    };

    const packages = [
        { id: 'one-hour-btn', type: 'one_hour', amount: 10, duration: 1, bandwidth: 5 },
        { id: 'two-hour-btn', type: 'two_hour', amount: 15, duration: 2, bandwidth: 5 },
        { id: 'four-hour-btn', type: 'four_hour', amount: 25, duration: 4, bandwidth: 5 },
        { id: 'six-hour-btn', type: 'six_hour', amount: 30, duration: 6, bandwidth: 5 },
        { id: 'one-day-btn', type: 'one_day', amount: 40, duration: 24, bandwidth: 5 },
        { id: 'two-day-btn', type: 'two_day', amount: 70, duration: 48, bandwidth: 5 },
        { id: 'weekly-btn', type: 'weekly', amount: 250, duration: 168, bandwidth: 5 },
        { id: 'monthly-btn', type: 'monthly', amount: 900, duration: 720, bandwidth: 5 }
    ];

    packages.forEach(pkg => {
        document.getElementById(pkg.id).addEventListener('click', async (event) => {
            try {
                const response = await fetch(`${baseUrl}/package/${pkg.type}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                });
                const result = await response.json();
                if (response.ok) {
                    showMessage(result.message, false, event);
                    showPaymentPrompt(pkg.type, pkg.amount, pkg.duration, pkg.bandwidth);
                } else {
                    showMessage(result.message, true, event);
                }
            } catch (error) {
                showMessage(" server request failure, try again later " + error.message, true, event);
            }
        });
    });

    document.getElementById('login-btn').addEventListener('click', async (event) => {
        const username = document.querySelector('input[type="text"][placeholder="Username"]').value.trim();
        const password = document.querySelector('input[type="password"]').value;
        if (!username || !password) {
            showMessage("Please enter both username and password", true, event);
            return;
        }
        try {
            const response = await fetch(`${baseUrl}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const result = await response.json();
            showMessage(result.message, !response.ok, event);
        } catch (error) {
            showMessage("Failed to login: " + error.message, true, event);
        }
    });

    document.getElementById('create-account-btn').addEventListener('click', async (event) => {
        const username = document.querySelector('input[type="text"][placeholder="Username"]').value.trim();
        const password = document.querySelector('input[type="password"]').value;
        if (!username || !password) {
            showMessage("Please enter both username and password", true, event);
            return;
        }
        try {
            const response = await fetch(`${baseUrl}/create_account`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const result = await response.json();
            showMessage(result.message, !response.ok, event);
        } catch (error) {
            showMessage("Failed to create account: " + error.message, true, event);
        }
    });

    document.getElementById('activate-btn').addEventListener('click', async (event) => {
        const voucherCode = document.getElementById('voucher-code').value.trim();
        if (!voucherCode) {
            showMessage("Please enter a voucher code", true, event);
            return;
        }
        try {
            const response = await fetch(`${baseUrl}/activate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ voucherCode })
            });
            const result = await response.json();
            showMessage(result.message, !response.ok, event);
        } catch (error) {
            showMessage("Failed to activate voucher: " + error.message, true, event);
        }
    });
});