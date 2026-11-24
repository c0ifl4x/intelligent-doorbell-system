import adapter from '@sveltejs/adapter-static';
import { sveltekit } from '@sveltejs/kit/vite';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';
import type { UserConfig } from 'vite';

export default {
  plugins: [
    sveltekit({
      preprocess: vitePreprocess(),
      adapter: adapter({
        fallback: 'index.html'
      })
    })
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
} satisfies UserConfig;
