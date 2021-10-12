import counterpart from 'counterpart';

export const setupCounterpart = () => {
  console.log('Setting up counterpart defaults...');

  counterpart.setMissingEntryGenerator(function (key) {
    // eslint-disable-next-line no-console
    console.error(`Missing translation: ${key}`);
    return `${key}`;
  });

  counterpart.registerTranslations('en', {
    appName: 'Picking',
    general: {
      Product: 'Product',
      Locator: 'Locator',
      Barcode: 'Barcode',
      QtyToPick: 'Qty to pick',
      QtyPicked: 'Qty picked',
      PleaseTryAgain: 'Please try again',
    },
    login: {
      submitButton: 'Login',
    },
    activities: {
      scanBarcode: {
        defaultCaption: 'Scan',
        invalidScannedBarcode: 'Scanned code is invalid',
      },
      picking: {
        PickingLine: 'Picking Line',
        scanHUBarcode: 'Scan HU',
        notEligibleHUBarcode: 'HU barcode not matching',
        invalidQtyPicked: 'Invalid qty picked',
        quantityToPick: 'Quantity to pick',
        confirmDone: 'Done',
      },
    },
  });

  counterpart.registerTranslations('de', {
    appName: 'Kommissionierung',
    general: {
      Product: 'Produkt',
      Locator: 'Lagerort',
      Barcode: 'Barcode',
      QtyToPick: 'Qty to pick',
      QtyPicked: 'Menge (Lagereinheit)',
      PleaseTryAgain: 'Please try again',
    },
    login: {
      submitButton: 'Login',
    },
    activities: {
      scanBarcode: {
        defaultCaption: 'Scan',
        invalidScannedBarcode: 'Scanned code is invalid',
      },
      picking: {
        PickingLine: 'Picking Line',
        scanHUBarcode: 'Scan HU',
        notEligibleHUBarcode: 'HU barcode not matching',
        invalidQtyPicked: 'Invalid qty picked',
        quantityToPick: 'Quantity to pick',
        confirmDone: 'Done',
      },
    },
  });

  // setLanguage('de_DE');
};

export const setLanguage = (language) => {
  const idx = language.indexOf('_');
  if (idx > 0) {
    language = language.substr(0, idx);
  }

  console.log('Setting language to ', language);
  counterpart.setLocale(language);
};
