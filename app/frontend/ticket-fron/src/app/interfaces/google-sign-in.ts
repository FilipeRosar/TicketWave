export interface GoogleChallenge {
  clientId: string;
  nonce: string;
}

export interface GoogleSignInSdk {
  initialize(options: {
    client_id: string;
    nonce: string;
    callback: (response: { credential: string }) => void;
  }): void;
  renderButton(container: HTMLElement, options: { theme: 'outline'; size: 'large'; width: string; locale: 'pt-BR' }): void;
}

export interface GoogleSignInWindow extends Window {
  google?: { accounts: { id: GoogleSignInSdk } };
}
