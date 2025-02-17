import axios from "axios";
// import cheerio from "cheerio";
import * as cheerio from "cheerio";

async function scrapeSkyland(query) {
  const url = `https://www.skyland.com.bd/index.php?route=product/search&search=${query}`;

  try {
    const { data } = await axios.get(url);
    const $ = cheerio.load(data); // Load the HTML data with Cheerio

    // Extract product details from the search results
    const products = [];
    $(".product-thumb").each((index, element) => {
      const productName = $(element).find(".name a").text().trim();
      const productDetails = $(element).find(".name a").attr("href");
      const productPrice = $(element)
        .find(".price .price-new")
        .text()
        .replace(/[^\d,]/g, "")
        .trim();
      const productImage = $(element)
        .find(".product-img .img-first")
        .attr("src");

      products.push({
        source: "Skyland",
        productName,
        productPrice,
        productDetails,
        productImage,
      });

      // Returning `false` breaks out of `.each` loop after 3 products
      if (products.length === 5) return false;
    });

    return products;
  } catch (error) {
    console.error(`Error scraping Skylad: ${error.message}`);
    return [];
  }
}

export { scrapeSkyland };
