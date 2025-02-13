import { scrapeTechland } from "./techland";
import { scrapeStartTech } from "./startTech";
import { scrapeRyans } from "./ryans";

async function scrapeAllSites(query) {
  const results = await Promise.allSettled([
    scrapeTechland(query),
    scrapeStartTech(query),
    scrapeRyans(query),
  ]);

  const allProducts = [];
  results.forEach((result) => {
    if (result.status === "fulfilled") {
      allProducts.push(...result.value.slice(0, 5));
    }
  });

  return allProducts;
}

export { scrapeAllSites };
