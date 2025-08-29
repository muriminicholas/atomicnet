document.addEventListener('DOMContentLoaded', function () {
    const baseUrl = 'https://atomichotspotapplication.onrender.com/api';

    /** -------------------------
     * Utility: Show Message
     ------------------------- */
    const showMessage = (text, isError = false, event = null) => {
        let messageDiv = document.getElementById('messageDiv');
        if (messageDiv) messageDiv.remove();

        messageDiv = document.createElement('div');
        messageDiv.id = 'messageDiv';
        messageDiv.textContent = text;
        messageDiv.className = `p-4 rounded-lg shadow-lg transition-opacity duration-300 ${
            isError ? 'bg-red-100 text-red-500' : 'bg-green-100 text-green-800'
        }`;
        messageDiv.style.position = 'absolute';
        messageDiv.style.zIndex = '1000';

        if (event) {
            const buttonRect = event.target.getBoundingClientRect();
            const buttonTop = buttonRect.top + window.scrollY;
            const buttonLeft = buttonRect.left + window.scrollX;
            const buttonWidth = buttonRect.width;

            messageDiv.style.top = `${buttonTop - 60}px`;
            messageDiv.style.left = `${buttonLeft + buttonWidth / 2}px`;
            messageDiv.style.transform = 'translateX(-50%)';
        } else {
            messageDiv.style.top = '20px';
            messageDiv.style.left = '50%';
            messageDiv.style.transform = 'translateX(-50%)';
        }

        document.body.appendChild(messageDiv);
        setTimeout(() => {
            messageDiv.style.opacity = '0';
            setTimeout(() => messageDiv.remove(), 300);
        }, 3000);
    };

    /** -------------------------
     * Payment Modal
     ------------------------- */
    const showPaymentPrompt = (packageInfo) => {
        const modal = document.createElement('div');
        modal.id = 'payment-modal';
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
        modal.innerHTML = `
            <div class="bg-white p-6 rounded-lg shadow-lg max-w-sm w-full">
                <h2 class="text-lg font-semibold mb-4 text-pink-600">
                    Payment for ${packageInfo.type.replace('_', ' ')}
                </h2>
                <p class="mb-2 text-gray-700">
                    Package: <strong>${packageInfo.durationHours} hours</strong>, <strong>${packageInfo.bandwidthMbps} Mbps</strong>
                </p>
                <p class="mb-4">Enter your MPESA phone number (e.g., +2547XXXXXXXX or 07XXXXXXXX):</p>
                <input id="mpesa-phone" type="text" placeholder="e.g., +254712345678 or 0712345678"
                    class="w-full mb-4 p-2 border rounded focus:ring-2 focus:ring-pink-300" />
                <p class="mb-4 font-semibold">Amount: Ksh.${packageInfo.price}</p>
                <div class="flex gap-4">
                    <button id="pay-button"
                        class="bg-gradient-to-r from-green-400 to-green-500 text-white p-2 rounded w-full hover:opacity-90">
                        Pay
                    </button>
                    <button id="close-modal"
                        class="bg-gray-200 text-gray-800 p-2 rounded w-full hover:bg-gray-300">
                        Cancel
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        document.getElementById('pay-button').addEventListener('click', async (event) => {
            let phoneNumber = document.getElementById('mpesa-phone').value.trim();
            if (!phoneNumber) {
                showMessage("Please enter a valid MPESA phone number (e.g., +2547XXXXXXXX or 07XXXXXXXX)", true, event);
                return;
            }

            // Normalize phone number: convert 07XXXXXXXX to +2547XXXXXXXX
            if (/^07\d{8}$/.test(phoneNumber)) {
                console.log(`Normalizing phone number from ${phoneNumber} to +254${phoneNumber.substring(1)}`);
                phoneNumber = '+254' + phoneNumber.substring(1); // Convert 0712345678 to +254712345678
            }

            // Validate phone number format
            if (!/^\+2547\d{8}$/.test(phoneNumber)) {
                showMessage("Please enter a valid MPESA phone number (e.g., +2547XXXXXXXX or 07XXXXXXXX)", true, event);
                return;
            }

            try {
                const response = await fetch(`${baseUrl}/initiate_payment`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ phoneNumber, packageType: packageInfo.type, amount: packageInfo.price }),
                });

                let errorData = {};
                try {
                    errorData = await response.json();
                } catch (jsonError) {
                    console.error("Failed to parse JSON response:", jsonError);
                    throw new Error(`Server returned invalid response: HTTP ${response.status}`);
                }

                if (!response.ok) {
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }

                const result = errorData; // Reuse parsed JSON
                showMessage(result.message || "A request is sent to your phone. Enter your MPESA PIN.", false, event);
                modal.remove();
            } catch (error) {
                showMessage(`Failed to initiate payment: ${error.message}`, true, event);
            }
        });

        document.getElementById('close-modal').addEventListener('click', () => modal.remove());
    };

    /** -------------------------
     * Packages
     ------------------------- */
    const packages = [
        { id: 'one-hour-btn', type: 'one_hour', amount: 10, duration: 1, bandwidth: 5 },
        { id: 'two-hour-btn', type: 'two_hour', amount: 15, duration: 2, bandwidth: 5 },
        { id: 'four-hour-btn', type: 'four_hour', amount: 25, duration: 4, bandwidth: 5 },
        { id: 'six-hour-btn', type: 'six_hour', amount: 30, duration: 6, bandwidth: 5 },
        { id: 'one-day-btn', type: 'one_day', amount: 40, duration: 24, bandwidth: 5 },
        { id: 'two-day-btn', type: 'two_day', amount: 70, duration: 48, bandwidth: 5 },
        { id: 'weekly-btn', type: 'weekly', amount: 250, duration: 168, bandwidth: 5 },
        { id: 'monthly-btn', type: 'monthly', amount: 900, duration: 720, bandwidth: 5 },
    ];

    packages.forEach(pkg => {
        const btn = document.getElementById(pkg.id);
        if (!btn) return;
        btn.addEventListener('click', async (event) => {
            try {
                const response = await fetch(`${baseUrl}/package/${pkg.type}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }

                const result = await response.json();
                showPaymentPrompt(result.data || pkg); // Use backend data if available
            } catch (error) {
                showMessage(`Package request failed: ${error.message}`, true, event);
            }
        });
    });

    /** -------------------------
     * Login
     ------------------------- */
    const loginBtn = document.getElementById('login-btn');
    if (loginBtn) {
        loginBtn.addEventListener('click', async (event) => {
            const username = document.getElementById('login-username').value.trim();
            const password = document.getElementById('login-password').value;
            if (!username || !password) {
                showMessage("Please enter both username and password", true, event);
                return;
            }
            try {
                const response = await fetch(`${baseUrl}/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ username, password }),
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }

                const result = await response.json();
                showMessage(result.message || "Login successful", false, event);
            } catch (error) {
                showMessage(`Failed to login: ${error.message}`, true, event);
            }
        });
    }

    /** -------------------------
     * Create Account
     ------------------------- */
    const createBtn = document.getElementById('create-account-btn');
    if (createBtn) {
        createBtn.addEventListener('click', async (event) => {
            const username = document.getElementById('login-username').value.trim();
            const password = document.getElementById('login-password').value;
            if (!username || !password) {
                showMessage("Please enter both username and password", true, event);
                return;
            }
            try {
                const response = await fetch(`${baseUrl}/create_account`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ username, password }),
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }

                const result = await response.json();
                showMessage(result.message || "Account created successfully", false, event);
            } catch (error) {
                showMessage(`Failed to create account: ${error.message}`, true, event);
            }
        });
    }

    /** -------------------------
     * Activate Voucher
     ------------------------- */
    const activateBtn = document.getElementById('activate-btn');
    if (activateBtn) {
        activateBtn.addEventListener('click', async (event) => {
            const voucherCode = document.getElementById('voucher-code').value.trim();
            if (!voucherCode) {
                showMessage("Please enter a voucher code", true, event);
                return;
            }
            try {
                const response = await fetch(`${baseUrl}/activate`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ voucherCode }),
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }

                const result = await response.json();
                showMessage(result.message || "Voucher activated successfully", false, event);
            } catch (error) {
                showMessage(`Failed to activate voucher: ${error.message}`, true, event);
            }
        });
    }

    /** -------------------------
     * Reconnect (MPESA Transaction Code)
     ------------------------- */
    const reconnectBtn = document.getElementById('reconnect-btn');
    if (reconnectBtn) {
        reconnectBtn.addEventListener('click', async (event) => {
            const mpesaCode = document.getElementById('mpesa-transaction-code').value.trim();
            if (!mpesaCode) {
                showMessage("Please enter an MPESA transaction code", true, event);
                return;
            }
            try {
                const response = await fetch(`${baseUrl}/reconnect`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ mpesaCode }),
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }

                const result = await response.json();
                showMessage(result.message || "Reconnect successful", false, event);
            } catch (error) {
                showMessage(`Reconnect failed: ${error.message}`, true, event);
            }
        });
    }
});