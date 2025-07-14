{#for portal in portals}
-- CFP Config for {portal.portalName} portal
INSERT INTO portals (portal_name, base_url, portal_type, description) VALUES ('{portal.portalName}', '{portal.baseUrl}', '{portal.portalType}', '{portal.description}');

{/for}