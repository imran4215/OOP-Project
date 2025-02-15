import { scrapeAllSites } from "./scrapeAllSites.js";
import stringSimilarity from "string-similarity";

// Normalize product names
function normalizeProductName(name) {
  return name
    .toLowerCase() // Convert to lowercase
    .replace(/\b8g\b/g, "8gb") // Replace "8G" with "8GB"
    .replace(/\b6g\b/g, "6gb") // Replace "6G" with "6GB"
    .replace(/\boc\b/g, "overclock") // Normalize "OC" to "overclock"
    .replace(/[^a-z0-9\s]/g, "") // Remove special characters
    .replace(/\s+/g, " ") // Normalize spaces
    .trim(); // Remove leading/trailing spaces
}

// Function to find or create a group for a product name
function findOrCreateGroup(groupedProducts, productName, threshold = 0.8) {
  const normalizedName = normalizeProductName(productName);
  const groupKeys = Object.keys(groupedProducts);

  if (groupKeys.length > 0) {
    // Use string-similarity to find the best match
    const matches = stringSimilarity.findBestMatch(
      normalizedName,
      groupKeys.map(normalizeProductName)
    );
    const bestMatch = matches.bestMatch;

    // If the best match is above the threshold, use that group key
    if (bestMatch.rating >= threshold) {
      return groupKeys.find(
        (key) => normalizeProductName(key) === bestMatch.target
      );
    }
  }

  // If no match is found, create a new group
  return productName;
}

async function scrapeAndSort(query) {
  const products = await scrapeAllSites(query);

  const groupedProducts = {};
  products.forEach((product) => {
    // Find or create a group for the product name
    const groupKey = findOrCreateGroup(groupedProducts, product.productName);
    if (!groupedProducts[groupKey]) {
      groupedProducts[groupKey] = [];
    }
    groupedProducts[groupKey].push(product);
  });

  // Sort prices for each group
  for (const key in groupedProducts) {
    groupedProducts[key].sort((a, b) => a.price - b.price);
  }

  return groupedProducts;
}

export { scrapeAndSort };
