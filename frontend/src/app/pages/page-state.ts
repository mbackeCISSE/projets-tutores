import { messageErreur } from '../services/api.service';
export class PageState {
  loading = false;
  busy = false;
  error = '';
  success = '';
  async charger(action: () => Promise<void>): Promise<void> {
    this.loading = true;
    this.error = '';
    try { await action(); } catch (error: unknown) { this.error = messageErreur(error); }
    finally { this.loading = false; }
  }
  async envoyer(action: () => Promise<void>): Promise<void> {
    if (this.busy) return;
    this.busy = true;
    this.error = '';
    this.success = '';
    try { await action(); } catch (error: unknown) { this.error = messageErreur(error); }
    finally { this.busy = false; }
  }
}