const API_BASE = 'http://localhost:8080/api';

function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
}

export async function login(username, password) {
  const response = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error('Invalid credentials');
  }

  return response.json();
}

export async function createTransaction(cardNumber, pin, amount, type) {
  const response = await fetch(`${API_BASE}/system1/transactions`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ cardNumber, pin, amount, type }),
  });

  return response.json();
}

export async function getTransactions() {
  const response = await fetch(`${API_BASE}/system1/transactions`, {
    headers: getAuthHeaders(),
  });

  return response.json();
}

export async function getCardBalances() {
  const response = await fetch(`${API_BASE}/cards/balance`, {
    headers: getAuthHeaders(),
  });

  return response.json();
}
