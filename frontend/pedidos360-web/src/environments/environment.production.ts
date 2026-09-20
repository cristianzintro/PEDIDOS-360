export const environment = {
  production: true,
  apiUrl: 'https://YOUR_API_GATEWAY_URL',
  msal: {
    tenantId: 'YOUR_TENANT_ID',
    clientId: 'YOUR_SPA_CLIENT_ID',
    authority: 'https://login.microsoftonline.com/YOUR_TENANT_ID',
    redirectUri: 'https://YOUR_FRONTEND_DOMAIN',
    apiScope: 'api://YOUR_API_CLIENT_ID/access_as_user',
  },
};
