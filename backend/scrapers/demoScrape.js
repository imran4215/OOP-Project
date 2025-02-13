import axios from "axios";
// import cheerio from "cheerio";
import * as cheerio from "cheerio";

async function demoScrape(query) {
  const url = `https://www.startech.com.bd/product/search?search=${query}`;

  try {
    const { data } = await axios.get(url);
    const $ = cheerio.load(data); // Load the HTML data with Cheerio
    console.log(url);

    // Extract product names with the class 'p-item-name'
    const products = [];
    $(".p-item").each((index, element) => {
      const productName = $(element).find(".p-item-name a").text().trim();
      const productDetails = $(element).find(".p-item-name a").attr("href");
      const productPrice = $(element).find(".p-item-price").text().trim();
      const productImage = $(element).find(".p-item-img img").attr("src");

      products.push({
        source: "StartTech",
        productName,
        productPrice,
        productDetails,
        productImage,
      });

      if (products.length === 5) return false;
    });

    return products;
  } catch (error) {
    console.error(`Error scraping StartTech: ${error.message}`);
    return [];
  }
}

export { demoScrape };
