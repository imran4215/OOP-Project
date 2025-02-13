const axios = require("axios");
const cheerio = require("cheerio");

async function scrapeStartTech(query) {
  const url = `https://www.startech.com.bd/product/search?search=${query}`;
  const products = [];

  try {
    const { data } = await axios.get(url);
    const $ = cheerio.load(data);

    $(".p-item").each((_, element) => {
      const name = $(element).find(".p-item-name a").text().trim();
      const price = parseFloat(
        $(element)
          .find(".price")
          .text()
          .replace(/[^0-9.]/g, "")
      );
      const link = $(element).find(".p-item-name a").attr("href");

      products.push({
        source: "StartTech",
        name,
        price,
        link: `https://www.startech.com.bd${link}`,
      });
    });

    return products;
  } catch (error) {
    console.error(`Error scraping StartTech: ${error.message}`);
    return [];
  }
}

module.exports = { scrapeStartTech };
