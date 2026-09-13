import { Injectable } from '@angular/core';
import * as CryptoJS from 'crypto-js';

@Injectable({
  providedIn: 'root',
})
export class LocalService {
  private key = '123';

  constructor() {}

  // Save encrypted data in localStorage
  public saveData(key: string, value: string) {
    if (value) {
      try {
        const encryptedValue = this.encrypt(value);
        localStorage.setItem(key, encryptedValue);
      } catch (error) {
        console.error('Error encrypting data:', error);
      }
    } else {
      console.error('Invalid value provided for encryption:', value);
    }
  }

  // Get decrypted data from localStorage
  public getData(key: string): string {
    try {
      let data = localStorage.getItem(key) || '';
      if (data) {
        return this.decrypt(data);
      } else {
        console.warn(`No data found for key: ${key}`);
        return '';
      }
    } catch (error) {
      console.error('Error decrypting data:', error);
      return '';
    }
  }

  // Remove data from localStorage
  public removeData(key: string) {
    localStorage.removeItem(key);
  }

  // Clear all data from localStorage
  public clearData() {
    localStorage.clear();
  }

  // Encrypt the given text using AES
  private encrypt(txt: string): string {
    try {
      return CryptoJS.AES.encrypt(txt, this.key).toString();
    } catch (error) {
      console.error('Error during encryption:', error);
      return '';
    }
  }

  // Decrypt the given text using AES
  private decrypt(txtToDecrypt: string): string {
    try {
      const bytes = CryptoJS.AES.decrypt(txtToDecrypt, this.key);
      const decryptedData = bytes.toString(CryptoJS.enc.Utf8);
      if (decryptedData) {
        return decryptedData;
      } else {
        console.warn('Decryption failed: Invalid data or incorrect key');
        return '';
      }
    } catch (error) {
      console.error('Error during decryption:', error);
      return '';
    }
  }
}
