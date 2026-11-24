<script lang="ts">
  import 'carbon-components-svelte/css/g90.css';
  import { Content, Header, HeaderNav, HeaderNavItem } from 'carbon-components-svelte';
  import { page } from '$app/state';

  let { children } = $props();
</script>

<Header companyName="Intelligent home automation" platformName="Relay server">
  <HeaderNav>
    <HeaderNavItem href="/" isSelected={page.url.pathname === '/'} text="System stats" />
  </HeaderNav>
</Header>

<Content>
  {@render children()}
</Content>
