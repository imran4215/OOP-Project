import { scrapeAllSites } from "./scrapeAllSites.js";

async function scrapeAndSort(query) {
  const products = await scrapeAllSites(query);

  // Group by product name
  const groupedProducts = {};
  products.forEach((product) => {
    if (!groupedProducts[product.productName]) {
      groupedProducts[product.productName] = [];
    }
    groupedProducts[product.productName].push(product);
  });

  // Sort prices for each group
  for (const key in groupedProducts) {
    groupedProducts[key].sort((a, b) => a.price - b.price);
  }

  return groupedProducts;
}

export { scrapeAndSort };
