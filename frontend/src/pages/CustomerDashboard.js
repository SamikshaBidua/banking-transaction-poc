import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTransactions, createTransaction, getCardBalances } from '../services/api';
import '../App.css';

function CustomerDashboard() {
  const [transactions, setTransactions] = useState([]);
  const [cards, setCards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cardNumber, setCardNumber] = useState('');
  const [pin, setPin] = useState('');
  const [amount, setAmount] = useState('');
  const [type, setType] = useState('topup');
  const [message, setMessage] = useState(null);
  const navigate = useNavigate();

  const username = localStorage.getItem('username');
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (!token) {
      navigate('/');
      return;
    }
    loadData();
  }, [token, navigate]);

  const loadData = async () => {
    try {
      const [txns, cardData] = await Promise.all([
        getTransactions(),
        getCardBalances()
      ]);
      setTransactions(txns);
      setCards(cardData);
    } catch (err) {
      console.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null);

    try {
      const response = await createTransaction(cardNumber, pin, parseFloat(amount), type);
      if (response.success) {
        setMessage({ type: 'success', text: `${type === 'topup' ? 'Top-up' : 'Withdrawal'} successful! New balance: $${response.newBalance?.toFixed(2)}` });
        setCardNumber('');
        setPin('');
        setAmount('');
        loadData();
      } else {
        setMessage({ type: 'error', text: response.message });
      }
    } catch (err) {
      setMessage({ type: 'error', text: 'Transaction failed. Please try again.' });
    }
  };

  const totalBalance = cards.reduce((sum, c) => sum + c.balance, 0);

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Banking POC - Customer</h1>
        <div className="user-info">
          <span>Welcome, {username}</span>
          <span className="badge badge-approved">CUSTOMER</span>
          <button className="logout-btn" onClick={handleLogout}>Logout</button>
        </div>
      </div>

      <div className="dashboard-content">
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-label">Total Balance</div>
            <div className="stat-value">${totalBalance.toFixed(2)}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Active Cards</div>
            <div className="stat-value">{cards.length}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Transactions</div>
            <div className="stat-value">{transactions.length}</div>
          </div>
        </div>

        {cards.length > 0 && (
          <div className="section">
            <h2>My Cards</h2>
            {cards.map(card => (
              <div key={card.id} className="card-balance">
                <span className="card-number">{card.cardNumber}</span>
                <span style={{ color: '#666', fontSize: 13 }}>{card.cardHolderName}</span>
                <span className="balance">${card.balance.toFixed(2)}</span>
              </div>
            ))}
          </div>
        )}

        <div className="section">
          <h2>New Transaction</h2>
          {message && (
            <div className={`alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
              {message.text}
            </div>
          )}
          <form onSubmit={handleSubmit} className="transaction-form">
            <div className="form-group">
              <label>Card Number</label>
              <input
                type="text"
                value={cardNumber}
                onChange={(e) => setCardNumber(e.target.value)}
                placeholder="e.g., 4111111111111111"
                required
              />
            </div>
            <div className="form-group">
              <label>PIN</label>
              <input
                type="password"
                value={pin}
                onChange={(e) => setPin(e.target.value)}
                placeholder="Enter PIN"
                required
                maxLength={4}
              />
            </div>
            <div className="form-group">
              <label>Amount ($)</label>
              <input
                type="number"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="0.00"
                required
                min="0.01"
                step="0.01"
              />
            </div>
            <div className="form-group">
              <label>Type</label>
              <select value={type} onChange={(e) => setType(e.target.value)}>
                <option value="topup">Top-Up</option>
                <option value="withdraw">Withdrawal</option>
              </select>
            </div>
            <div className="full-width form-actions">
              <button type="submit" className="btn btn-primary">Submit Transaction</button>
            </div>
          </form>
        </div>

        <div className="section">
          <h2>My Transactions</h2>
          {loading ? (
            <p>Loading...</p>
          ) : transactions.length === 0 ? (
            <p>No transactions yet.</p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Card Number</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Reason</th>
                    <th>Timestamp</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map(txn => (
                    <tr key={txn.id}>
                      <td>{txn.id}</td>
                      <td style={{ fontFamily: 'monospace' }}>{txn.cardNumber}</td>
                      <td>{txn.type.toUpperCase()}</td>
                      <td>${txn.amount.toFixed(2)}</td>
                      <td>
                        <span className={`badge ${txn.status === 'APPROVED' ? 'badge-approved' : 'badge-declined'}`}>
                          {txn.status}
                        </span>
                      </td>
                      <td>{txn.reason || '-'}</td>
                      <td>{new Date(txn.timestamp).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default CustomerDashboard;
