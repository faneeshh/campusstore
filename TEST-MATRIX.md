# Test Matrix

| ID  | Scenario | Steps | Expected | Actual |
|-----|----------|-------|----------|--------|
| T01 | Admin login | Log in as admin@example.com / Admin@1234, open /admin/dashboard | Admin dashboard loads with categories, products, orders | Admin dashboard loaded successfully showing all three sections |
| T02 | Admin creates category | Create a category named "Test Category"; confirm it appears in the list | Category appears in dashboard table | Category "Test Category" appeared immediately in the categories table |
| T03 | Admin product lifecycle | Create product → edit product → deactivate product; confirm it disappears from catalog | Product disappears from /catalog after deactivation | Product created, edited, deactivated; disappeared from catalog. isActive=false in DB |
| T04 | Customer registration | Register with killua@gmail.com; confirm redirect to login | Redirect to /login with success message | Redirected to /login?registered=true after successful registration |
| T05 | Catalog F/S/P | Use name filter, categoryId filter, inStock filter; change sortBy/sortDir; navigate pages | Filtered results update correctly; page 0 and page 1 both return results | All filters, sorts, and pagination worked correctly. Page 1 shows items 6-8 |
| T06 | Create multi-item order | Add qty > 0 for at least 2 products; place order; see confirmation | Confirmation page shows order ID and total | Order confirmed with ID and correct total shown on confirmation page |
| T07 | Stock deducted + totals correct | After placing order, check stockQty in admin and verify total | stockQty reduced by ordered qty; total matches server-computed value | Stock reduced correctly (e.g. 20→18); total matched sum of qty × unitPrice |
| T08 | My order history + details | Open /orders/history; click one order; confirm items and total match | Order list shows all orders; detail shows correct items and total | History listed all orders; detail page showed correct items, qty, unitPrice, total |
| T09 | Forbidden — wrong role | Log in as CUSTOMER; navigate to /admin/dashboard directly | HTTP 403 Forbidden page shown | 403 Forbidden page displayed when customer accessed /admin/dashboard |
| T10 | Forbidden — wrong owner | As Customer A, paste Customer B's order detail URL | HTTP 403 Forbidden page shown | 403 Forbidden page displayed when accessing another customer's order |
| T11 | Admin status update + cancel restores stock | Set order NEW→CANCELLED; confirm stock restored. Set order NEW→FULFILLED; confirm cannot revert | Stock restored on cancel; FULFILLED order cannot change status | Stock restored after cancellation; FULFILLED order showed no status change option |
