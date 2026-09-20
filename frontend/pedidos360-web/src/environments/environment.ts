export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  msal: {
    tenantId: 'YOUR_TENANT_ID',
    clientId: 'YOUR_SPA_CLIENT_ID',
    authority: 'https://login.microsoftonline.com/YOUR_TENANT_ID',
    redirectUri: 'http://localhost:4200',
    apiScope: 'api://YOUR_API_CLIENT_ID/access_as_user',
  },
};
