import { scrapeComputerVillage } from "./scrapeComputerVillage.js";
import { scrapeComputerVision } from "./scrapeComputerVision.js";
import { scrapeRyans } from "./scrapeRyans.js";
import { scrapeSkyland } from "./scrapeSkyLand.js";
import { scrapeStartTech } from "./scrapeStartTech.js";
import { scrapeTechland } from "./scrapeTechland.js";
import { scrapeUltraTech } from "./scrapeUltraTech.js";

async function scrapeAllSites(query) {
  // First, scrape StartTech to get the product names
  const startTechResults = await scrapeStartTech(query);

  // Extract product names from StartTech results
  const productNames = startTechResults.map((product) => product.productName);

  // Scrape other websites using the product names
  const allResults = await Promise.allSettled(
    productNames.map((productName) =>
      Promise.all([
        scrapeTechland(productName),
        scrapeRyans(productName),
        //scrapeSkyland(productName),
        //scrapeUltraTech(productName),
        //scrapeComputerVillage(productName),
        //scrapeComputerVision(productName),
      ])
    )
  );

  // Combine all results into a single array
  const allProducts = [...startTechResults];

  allResults.forEach((result) => {
    if (result.status === "fulfilled") {
      result.value.forEach((products) => {
        allProducts.push(...products);
      });
    }
  });

  return allProducts;
}

export { scrapeAllSites };
