import { Injectable } from '@angular/core';
import type { GoogleSignInSdk, GoogleSignInWindow } from '../interfaces/google-sign-in';

@Injectable({ providedIn: 'root' })
export class GoogleSignInSdkLoader {
  private loading?: Promise<GoogleSignInSdk>;

  load(): Promise<GoogleSignInSdk> {
    const browser = window as GoogleSignInWindow;
    const existing = browser.google?.accounts?.id;
    if (existing) return Promise.resolve(existing);

    this.loading ??= new Promise<GoogleSignInSdk>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.onload = () => {
        const sdk = browser.google?.accounts?.id;
        if (sdk) resolve(sdk);
        else reject(new Error('Google Identity Services indisponível.'));
      };
      script.onerror = () => reject(new Error('Google Identity Services indisponível.'));
      document.head.append(script);
    });
    return this.loading;
  }
}
