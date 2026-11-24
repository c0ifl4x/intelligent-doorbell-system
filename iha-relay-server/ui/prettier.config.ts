import type { PluginConfig } from '@ianvs/prettier-plugin-sort-imports';
import type { Config } from 'prettier';

const config: Config & PluginConfig = {
  useTabs: false,
  tabWidth: 2,
  singleQuote: true,
  trailingComma: 'none',
  printWidth: 100,
  plugins: ['prettier-plugin-svelte', '@ianvs/prettier-plugin-sort-imports'],
  overrides: [
    {
      files: '*.svelte',
      options: {
        parser: 'svelte'
      }
    }
  ],
  importOrder: [
    '<BUILTIN_MODULES>',
    '<THIRD_PARTY_MODULES>',
    '^\\$app/(.*)$',
    '^\\$env/(.*)$',
    '^\\$lib/(.*)$',
    '^[./]'
  ],
  importOrderParserPlugins: ['typescript', 'decorators-legacy']
};

export default config;
