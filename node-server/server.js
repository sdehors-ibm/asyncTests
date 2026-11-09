'use strict';

const http = require('node:http');

const HOST = '127.0.0.1';
const PORT = Number(process.env.PORT) || 8080;
const MAX_WAIT_MS = 60000;

const server = http.createServer((req, res) => {
  const url = new URL(req.url, `http://${HOST}:${PORT}`);

  if (req.method !== 'GET' || url.pathname !== '/hello-resteasy/wait') {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Not found');
    return;
  }

  const raw = url.searchParams.get('wait') ?? '0';
  const wait = Number(raw);
  if (!/^\d+$/.test(raw) || wait > MAX_WAIT_MS) {
    res.writeHead(400, { 'Content-Type': 'text/plain' });
    res.end('Invalid request');
    return;
  }

  const timer = setTimeout(() => {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end(`waited ${wait} ms`);
  }, wait);

  res.on('close', () => clearTimeout(timer));
});

server.listen(PORT, HOST, () => {
  console.log(`Listening on http://${HOST}:${PORT}/hello-resteasy/wait?wait=<ms>`);
});
