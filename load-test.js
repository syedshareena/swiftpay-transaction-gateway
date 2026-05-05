import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
  vus: 50,
  duration: '60s',
};

let counter = 0;

export default function () {
  counter++;

  const payload = JSON.stringify({
    transactionId: `txn-${__VU}-${__ITER}`,
    senderId: "550e8400-e29b-41d4-a716-446655440000",
    receiverId: "550e8400-e29b-41d4-a716-446655440001",
    amount: 1.00,
    currency: "USD"
  });

  const response = http.post(
    'http://localhost:8080/v1/payments',
    payload,
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(response, {
    'status is 202': (r) => r.status === 202,
  });

  sleep(0.2);
}