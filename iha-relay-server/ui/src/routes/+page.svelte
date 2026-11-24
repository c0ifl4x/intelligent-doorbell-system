<script lang="ts">
  import { Button, Column, Content, Grid, Row } from 'carbon-components-svelte';

  let status = $state('IDLE');

  function toggleStatus() {
    status = status === 'IDLE' ? 'RUNNING' : 'IDLE';
  }
</script>

<Content>
  <Grid>
    <Row>
      <Column>
        <p>System status: <strong>{status}</strong></p>
        <Button on:click={toggleStatus}>Toggle Status</Button>
      </Column>
    </Row>
  </Grid>
</Content>
