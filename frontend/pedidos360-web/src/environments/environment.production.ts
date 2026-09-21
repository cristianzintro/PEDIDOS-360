export const environment = {
  production: true,
  apiUrl: 'https://<API_GATEWAY_URL>',
  msal: {
    tenantId: '1e723c98-044a-4dc2-bbdf-ebef1ff41d29',
    clientId: '910a32fc-c921-4b26-a90f-ed127214f9bb',
    authority: 'https://login.microsoftonline.com/1e723c98-044a-4dc2-bbdf-ebef1ff41d29',
    redirectUri: 'https://<FRONTEND_URL>',
    apiScope: 'api://295f45be-4dfb-4387-9164-48ed7788f505/access_as_user',
  },
};
