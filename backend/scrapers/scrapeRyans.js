import axios from "axios";
// import cheerio from "cheerio";
import * as cheerio from "cheerio";

async function scrapeRyans(query) {
  const url = `https://www.ryans.com/search?q=${query}`;

  try {
    const { data } = await axios.get(url);

    const $ = cheerio.load(data); // Load the HTML data with Cheerio

    // Extract product names with the class 'p-item-name'
    const products = [];
    $(".p-item").each((index, element) => {
      const productName = $(element).find(".p-item-name a").text().trim();
      const productDetails = $(element).find(".p-item-name a").attr("href");
      const productPrice = $(element).find(".p-item-price").text().trim();

      products.push({
        source: "Ryans",
        productName,
        productPrice,
        productDetails,
      });

      // Returning `false` breaks out of `.each` loop after 3 products
      if (products.length === 3) return false;
    });

    return products;
  } catch (error) {
    console.error(`Error scraping StartTech: ${error.message}`);
    return [];
  }
}

export { scrapeRyans };
