import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTransactions } from '../services/api';
import '../App.css';

function AdminDashboard() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const username = localStorage.getItem('username');
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (!token) {
      navigate('/');
      return;
    }
    loadTransactions();
  }, [token, navigate]);

  const loadTransactions = async () => {
    try {
      const data = await getTransactions();
      setTransactions(data);
    } catch (err) {
      console.error('Failed to load transactions');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const totalTransactions = transactions.length;
  const approvedCount = transactions.filter(t => t.status === 'APPROVED').length;
  const declinedCount = transactions.filter(t => t.status === 'DECLINED').length;
  const totalVolume = transactions
    .filter(t => t.status === 'APPROVED')
    .reduce((sum, t) => sum + t.amount, 0);

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Banking POC - Super Admin</h1>
        <div className="user-info">
          <span>Welcome, {username}</span>
          <span className="badge badge-approved">ADMIN</span>
          <button className="logout-btn" onClick={handleLogout}>Logout</button>
        </div>
      </div>

      <div className="dashboard-content">
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-label">Total Transactions</div>
            <div className="stat-value">{totalTransactions}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Approved</div>
            <div className="stat-value" style={{ color: '#2e7d32' }}>{approvedCount}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Declined</div>
            <div className="stat-value" style={{ color: '#c62828' }}>{declinedCount}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Volume</div>
            <div className="stat-value">${totalVolume.toFixed(2)}</div>
          </div>
        </div>

        <div className="section">
          <h2>All Transactions</h2>
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
                    <th>User</th>
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
                      <td>{txn.userId}</td>
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

export default AdminDashboard;
